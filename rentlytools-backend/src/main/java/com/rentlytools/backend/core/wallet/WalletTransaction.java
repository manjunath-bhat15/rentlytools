package com.rentlytools.backend.core.wallet;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private Type type;

    private Double amount;

    @Column(name = "booking_id")
    private Long bookingId;

    // optional: for extra info (instead of description)
    @Column(columnDefinition = "json")
    private String meta;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Type {
        ADD, HOLD, RELEASE, DEBIT, CREDIT
    }
}