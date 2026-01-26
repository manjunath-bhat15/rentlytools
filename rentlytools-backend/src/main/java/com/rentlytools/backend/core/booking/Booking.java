package com.rentlytools.backend.core.booking;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "renter_id")
    private Long renterId;

    @Column(name = "listing_id")
    private Long listingId;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.REQ;

    @Column(name = "deposit_hold_txn_id")
    private Long depositHoldTxnId;

    @Column(name = "total_price")
    private Double totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum Status {
        REQ, ACC, DECL, ACTIVE, COMP, CAN
    }
}