package com.rentlytools.backend.admin;

import com.rentlytools.backend.verification.owner.OwnerVerification;
import com.rentlytools.backend.verification.owner.OwnerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/owner")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminOwnerController {

    private final OwnerVerificationRepository repo;

    // 🔹 Get all pending verifications
    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        List<OwnerVerification> list = repo.findByStatus(OwnerVerification.Status.PENDING);
        return ResponseEntity.ok(list);
    }

    // 🔹 Approve verification
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        var ov = repo.findById(id).orElse(null);
        if (ov == null)
            return ResponseEntity.status(404).body(Map.of("error", "Verification not found"));

        ov.setStatus(OwnerVerification.Status.APPROVED);
        ov.setReviewedBy(adminId);
        ov.setReviewedAt(LocalDateTime.now());
        repo.save(ov);

        return ResponseEntity.ok(Map.of("message", "Approved", "id", id));
    }

    // 🔹 Reject verification
    @PostMapping("/reject/{id}")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        var ov = repo.findById(id).orElse(null);
        if (ov == null)
            return ResponseEntity.status(404).body(Map.of("error", "Verification not found"));

        ov.setStatus(OwnerVerification.Status.REJECTED);
        ov.setReviewedBy(adminId);
        ov.setReviewedAt(LocalDateTime.now());
        repo.save(ov);

        return ResponseEntity.ok(Map.of("message", "Rejected", "id", id));
    }
}