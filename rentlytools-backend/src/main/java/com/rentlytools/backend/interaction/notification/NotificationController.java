package com.rentlytools.backend.interaction.notification;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final RedisTemplate<String, Object> redisTemplate;

    public NotificationController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 🔸 Send notification to a specific user
    @PostMapping("/send")
    public Map<String, Object> sendNotification(
            @RequestParam Long userId,
            @RequestParam String message
    ) {
        redisTemplate.convertAndSend("notifications", "user:" + userId + ":" + message);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "sent");
        res.put("userId", userId);
        res.put("message", message);
        return res;
    }

    // 🔸 Broadcast to everyone
    @PostMapping("/broadcast")
    public Map<String, Object> broadcast(@RequestParam String message) {
        redisTemplate.convertAndSend("notifications", message);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "broadcasted");
        res.put("message", message);
        return res;
    }
}