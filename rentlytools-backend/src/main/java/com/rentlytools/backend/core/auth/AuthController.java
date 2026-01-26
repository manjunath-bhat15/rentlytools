package com.rentlytools.backend.core.auth;

import com.rentlytools.backend.infrastructure.email.EmailService;
import com.rentlytools.backend.verification.owner.OwnerVerificationService;
import com.rentlytools.backend.infrastructure.security.JwtService;
import com.rentlytools.backend.core.user.User;
import com.rentlytools.backend.core.user.UserRepository;
import com.rentlytools.backend.shared.util.OtpUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final JwtService jwt;
    private final EmailService emailService;
    private final OtpUtil otpUtil;
    private final OwnerVerificationService ownerVerificationService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, String> otpStore = new HashMap<>();
    private final Set<String> verifiedEmails = new HashSet<>();

    /* =====================================================================
       REGISTER
       ===================================================================== */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("isOwner") String isOwnerStr,
            @RequestPart(value = "aadhaar", required = false) MultipartFile aadhaar,
            @RequestPart(value = "selfie", required = false) MultipartFile selfie,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng
    ) {
        log.info("Registration attempt for email: {}", email);
        try {
            boolean isOwner = Boolean.parseBoolean(isOwnerStr);
            log.debug("Params: name={}, isOwner={}, hasAadhaar={}, hasSelfie={}", 
                      name, isOwner, aadhaar != null, selfie != null);

            if (repo.findByEmail(email).isPresent()) {
                log.warn("Registration failed: Email {} already exists", email);
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(encoder.encode(password));
            user.setRoleUser(true);

            if (isOwner) {
                if (aadhaar == null || selfie == null) {
                    log.warn("Owner registration failed: Missing files for {}", email);
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Aadhaar + Selfie are required for owner registration"
                    ));
                }
                user.setRoleOwner(true);
                repo.save(user);
                log.info("User {} saved, starting owner verification...", email);

                ownerVerificationService.startVerification(user, aadhaar, selfie, lat, lng);
            } else {
                repo.save(user);
            }

            log.info("Registration successful for user: {}", email);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful",
                    "role_user", user.getRoleUser(),
                    "role_owner", user.getRoleOwner()
            ));

        } catch (Exception e) {
            log.error("Critical error during registration for {}: ", email, e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    /* =====================================================================
       LOGIN
       ===================================================================== */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        log.info("Login attempt for email: {}", req.email());
        
        var userOpt = repo.findByEmail(req.email());
        if (userOpt.isEmpty()) {
            log.warn("Login failed: User {} not found", req.email());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        var user = userOpt.get();
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            log.warn("Login failed: Incorrect password for {}", req.email());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        String token = jwt.generateToken(user.getEmail());
        log.info("Login successful for user: {}", req.email());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "name", user.getName(),
                "role_admin", user.getRoleAdmin(),
                "role_owner", user.getRoleOwner(),
                "role_user", user.getRoleUser()
        ));
    }

    /* =====================================================================
       SEND OTP
       ===================================================================== */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email, @RequestParam(required = false) String mode) {
        log.info("Requesting OTP for: {} (Mode: {})", email, mode);
        var userOpt = repo.findByEmail(email);

        if ("forgot".equals(mode) && userOpt.isEmpty()) {
            log.warn("OTP Send failed: Email {} not found for password reset", email);
            return ResponseEntity.badRequest().body(Map.of("message", "Email not registered."));
        }

        if (!"forgot".equals(mode) && userOpt.isPresent()) {
            log.warn("OTP Send failed: Email {} already registered", email);
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered."));
        }

        String otp = otpUtil.generateOtp();
        otpStore.put(email, otp);

        try {
            String recipientName = userOpt.map(User::getName).orElse("New User");
            emailService.sendOtpEmail(email, recipientName, otp);
            log.info("OTP sent successfully to {}", email);
        } catch (Exception e) {
            log.error("Email service failure for {}: ", email, e);
            return ResponseEntity.status(500).body(Map.of("message", "Failed to send OTP email"));
        }

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "email", email));
    }

    /* =====================================================================
       VERIFY OTP
       ===================================================================== */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        log.info("Verifying OTP for: {}", email);

        String storedOtp = otpStore.get(email);
        if (storedOtp == null) {
            log.warn("OTP verification failed: No OTP in store for {}", email);
            return ResponseEntity.badRequest().body(Map.of("message", "OTP expired or missing"));
        }

        if (!storedOtp.equals(otp)) {
            log.warn("OTP verification failed: Incorrect OTP for {}", email);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
        }

        otpStore.remove(email);
        verifiedEmails.add(email);
        log.info("OTP verified successfully for {}", email);

        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    /* =====================================================================
       RESET PASSWORD
       ===================================================================== */
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String newPass = req.get("password");
        log.info("Attempting password reset for: {}", email);

        if (!verifiedEmails.contains(email)) {
            log.warn("Reset blocked: Email {} not verified via OTP", email);
            return ResponseEntity.badRequest().body(Map.of("message", "OTP not verified"));
        }

        var user = repo.findByEmail(email).orElse(null);
        if (user == null) {
            log.error("Reset failed: User {} disappeared from DB during process", email);
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        user.setPasswordHash(encoder.encode(newPass));
        repo.save(user);
        verifiedEmails.remove(email);
        
        log.info("Password successfully reset for {}", email);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    // --- Global Error Handler for this Controller ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception caught in AuthController: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error", "details", ex.getMessage()));
    }
}