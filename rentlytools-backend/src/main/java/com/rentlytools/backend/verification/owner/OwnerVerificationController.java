package com.rentlytools.backend.verification.owner;

import com.rentlytools.backend.core.user.User;
import com.rentlytools.backend.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/verify")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OwnerVerificationController {

    private final OwnerVerificationService service;
    private final UserRepository userRepository;

    // --------------------------------------------------
    // START VERIFICATION
    // --------------------------------------------------
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> startVerification(
            @RequestParam("userId") Long userId,
            @RequestPart(value = "aadhaar", required = false) MultipartFile aadhaar,
            @RequestPart(value = "selfie", required = false) MultipartFile selfie,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng
    ) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body("userId is required");
            }

            var userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User user = userOpt.get();
            service.startVerification(user, aadhaar, selfie, lat, lng);

            return ResponseEntity.ok("Verification started");

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }

    // --------------------------------------------------
    // STATUS CHECK (NULL-SAFE)
    // --------------------------------------------------
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Long userId) {
        try {
            var ov = service.getVerificationByUserId(userId);

            if (ov == null) {
                return ResponseEntity.ok(Map.of("status", "NOT_SUBMITTED"));
            }

            Map<String, Object> map = new HashMap<>();
            map.put("status", ov.getStatus().name());
            map.put("aadhaarImageUrl", ov.getAadhaarImageUrl());
            map.put("selfieUrl", ov.getSelfieUrl());
            map.put("name", ov.getName());
            map.put("aadhaarNumber", ov.getAadhaarNumber());
            map.put("dob", ov.getDob());
            map.put("gender", ov.getGender());
            map.put("address", ov.getAddress());
            map.put("faceScore", ov.getFaceScore());
            map.put("reviewedAt", ov.getReviewedAt());
            map.put("reviewedBy", ov.getReviewedBy());

            return ResponseEntity.ok(map);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
}