package com.rentlytools.backend.interaction.notification;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final StringRedisTemplate redis;

    public NotificationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // publish to a "notifications" channel (or whatever you used in listener)
    public void sendToUser(Long userId, String message) {
        String payload = (userId == null) ? message : ("user:" + userId + ":" + message);
        redis.convertAndSend("notifications", payload);
    }
}