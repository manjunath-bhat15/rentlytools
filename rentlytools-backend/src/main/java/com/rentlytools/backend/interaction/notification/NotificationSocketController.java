package com.rentlytools.backend.interaction.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // 🔸 Send private notification to a user
    public void sendToUser(Long userId, String message) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                message
        );
        System.out.println("📨 Sent private notification to user " + userId + ": " + message);
    }

    // 🔸 Broadcast to all (optional)
    public void sendToAll(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
        System.out.println("📢 Broadcast notification: " + message);
    }
}