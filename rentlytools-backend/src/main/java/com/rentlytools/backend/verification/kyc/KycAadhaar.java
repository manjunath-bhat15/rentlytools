package com.rentlytools.backend.verification.kyc;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "kyc_aadhaar")
@Data
public class KycAadhaar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String aadhaarNumber;
    private String aadhaarName;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
