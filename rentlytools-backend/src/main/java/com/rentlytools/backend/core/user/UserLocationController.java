package com.rentlytools.backend.core.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/location")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserLocationController {

    private final UserRepository userRepo;

    @PostMapping("/update")
    public Map<String, Object> updateLocation(
            @RequestParam Long userId,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam String address
    ) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLat(lat);
        user.setLng(lng);
        user.setAddress(address);

        userRepo.save(user);

        return Map.of(
                "status", "SUCCESS",
                "message", "Location updated",
                "address", address
        );
    }
}