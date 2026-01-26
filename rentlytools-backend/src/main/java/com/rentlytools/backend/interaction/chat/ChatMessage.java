package com.rentlytools.backend.interaction.chat;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private Long senderId;
    private Long receiverId;
    private String message;

    private LocalDateTime sentAt = LocalDateTime.now();
}