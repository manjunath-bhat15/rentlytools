package com.rentlytools.backend.core.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
// Ensure this matches your Vite port (5173 or 5174)
@CrossOrigin(origins = "*") 
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestParam Long id) {
        return userRepo.findById(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("name", user.getName());
                    response.put("email", user.getEmail());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}