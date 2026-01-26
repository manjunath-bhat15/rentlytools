package com.rentlytools.backend.verification.owner;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerVerificationRepository extends JpaRepository<OwnerVerification, Long> {

    // ✔ Required for /owner/status API
   OwnerVerification findTopByUserIdOrderByIdDesc(Long userId);

    // ✔ Keep this only if your admin page uses it.
    // OwnerVerification.Status = PENDING, APPROVED, REJECTED
    java.util.List<OwnerVerification> findByStatus(OwnerVerification.Status status);
}