package com.rentlytools.backend.interaction.notification;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String message;
    private String type; // e.g. BOOKING_APPROVED
    private boolean readFlag = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}