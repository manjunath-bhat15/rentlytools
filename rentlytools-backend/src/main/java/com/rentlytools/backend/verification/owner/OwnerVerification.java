package com.rentlytools.backend.verification.owner;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "owner_verification")
public class OwnerVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to User
    @Column(name = "user_id")
    private Long userId;

    // Aadhaar details
    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    private String name;
    private String dob;
    private String gender;

    // Uploaded files
    @Column(name = "aadhaar_image_url")
    private String aadhaarImageUrl;

    @Column(name = "selfie_image_url")
    private String selfieUrl;

    @Column(columnDefinition = "TEXT")
    private String ocrText;

    // Face match results
    @Column(name = "face_score")
    private Double faceScore;

    @Column(name = "face_embedding_hash", length = 500)
    private String faceEmbeddingHash;

    // GPS location
    private Double lat;
    private Double lng;

    // NEW — full address (reverse geocoded)
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    // Status
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    // Admin review
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}