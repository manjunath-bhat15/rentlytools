package com.rentlytools.backend.interaction.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = new String(message.getBody());
        System.out.println("📨 Redis -> WebSocket message: " + msg);
        messagingTemplate.convertAndSend("/topic/chat", msg);
    }
}