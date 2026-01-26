package com.rentlytools.backend.interaction.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository chatRepo;
    private final RedisMessagePublisher redisPublisher;

    @GetMapping("/history")
    public List<ChatMessage> getChat(@RequestParam Long bookingId) {
        return chatRepo.findByBookingIdOrderBySentAtAsc(bookingId);
    }

    @MessageMapping("/send")
    public void send(ChatMessage msg) {
        chatRepo.save(msg);
        redisPublisher.publish("chat", msg);
    }

    // ✅ REST bridge for curl testing
    @PostMapping("/broadcast")
    public Object broadcastMessage(@RequestParam String msg) {
        System.out.println("🌐 Broadcasting from REST: " + msg);
        redisPublisher.publish("chat", msg);
        return Map.of("message", "Sent: " + msg);
    }
}