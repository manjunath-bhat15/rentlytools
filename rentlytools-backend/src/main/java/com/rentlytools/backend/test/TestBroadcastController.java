package com.rentlytools.backend.test;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestBroadcastController {

    private final SimpMessagingTemplate messagingTemplate;

    // You can trigger this endpoint manually
    @PostMapping("/broadcast")
    public String sendTestBroadcast(@RequestParam(defaultValue = "5") int activeBookings,
                                    @RequestParam(defaultValue = "2000") double walletBalance,
                                    @RequestParam(defaultValue = "3") int pendingRequests,
                                    @RequestParam(defaultValue = "7") int unreadMessages) {

        Map<String, Object> update = Map.of(
                "activeBookings", activeBookings,
                "walletBalance", walletBalance,
                "pendingRequests", pendingRequests,
                "unreadMessages", unreadMessages
        );

        messagingTemplate.convertAndSend("/topic/dashboard", update);
        return "✅ Broadcast sent: " + update;
    }
}