package com.rentlytools.backend.verification.kyc;

import com.rentlytools.backend.core.user.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
public class KycController {

    private final KycAadhaarRepository kycRepo;
    private final UserRepository userRepo;

    public KycController(KycAadhaarRepository kycRepo, UserRepository userRepo) {
        this.kycRepo = kycRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/approve/{userId}")
    public String approve(@PathVariable Long userId) {

        var user = userRepo.findById(userId).orElse(null);
        if(user == null) return "invalid user";

        user.setRoleOwner(true);
        userRepo.save(user);

        return "owner approved";
    }
}
