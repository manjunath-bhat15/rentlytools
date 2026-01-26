package com.rentlytools.backend.verification.kyc;

import com.rentlytools.backend.core.user.User;
import com.rentlytools.backend.core.user.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.io.ByteArrayResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/kyc")
public class KycSubmitController {

    private final KycAadhaarRepository kycRepo;
    private final UserRepository userRepo;
    private final WebClient client = WebClient.builder().baseUrl("http://localhost:5001").build();

    public KycSubmitController(KycAadhaarRepository kycRepo, UserRepository userRepo) {
        this.kycRepo = kycRepo;
        this.userRepo = userRepo;
    }

    @PostMapping(value="/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String submit(@RequestParam("userId") Long userId,
                         @RequestParam("file") MultipartFile file) throws Exception {

        byte[] bytes = file.getBytes();

        var formData = new LinkedMultiValueMap<String, Object>();
        formData.add("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "aadhaar.jpg";
            }
        });

        var response = client.post()
                .uri("/ocr")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // extract 12 digit aadhaar
        Matcher m = Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\b").matcher(response);
        String aadhaarNumber = m.find() ? m.group().replace(" ", "") : "NA";

        var user = userRepo.findById(userId).orElse(null);
        if (user == null) return "invalid user";

        // save kyc record
        KycAadhaar k = new KycAadhaar();
        k.setUserId(userId);
        k.setAadhaarNumber(aadhaarNumber);
        k.setAadhaarName(user.getName());
        k.setStatus(KycAadhaar.Status.APPROVED);
        kycRepo.save(k);

        // upgrade role
        user.setRoleOwner(true);
        userRepo.save(user);

        return "KYC done & owner approved";
    }
}
