package com.rentlytools.backend.interaction.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String channel, Object message) {
        System.out.println("📡 Publishing to Redis channel: " + channel + " -> " + message);
        redisTemplate.convertAndSend(channel, message.toString());
    }
}