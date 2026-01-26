package com.rentlytools.backend.verification.owner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentlytools.backend.core.user.User;
import com.rentlytools.backend.infrastructure.cloudinary.CloudinaryService;
import com.rentlytools.backend.verification.ocr.AadhaarExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OwnerVerificationService {

    private final OwnerVerificationRepository repo;
    private final CloudinaryService cloudinaryService;
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ocr.base-url}")
    private String ocrBaseUrl;

    // =====================================================
    // START VERIFICATION
    // =====================================================
    public void startVerification(
            User user,
            MultipartFile aadhaar,
            MultipartFile selfie,
            Double lat,
            Double lng
    ) throws Exception {

        if (aadhaar == null || selfie == null) {
            throw new IllegalArgumentException("Aadhaar and selfie are required");
        }

        // 1️⃣ Upload images
        String aadhaarUrl = cloudinaryService.upload(aadhaar);
        String selfieUrl = cloudinaryService.upload(selfie);

        // 2️⃣ OCR
        String ocrText = callPythonOCR(aadhaar);
        System.out.println("DEBUG OCR TEXT >>> " + ocrText);

        // 3️⃣ Parse Aadhaar
        var info = AadhaarExtractor.extract(ocrText);

        // 4️⃣ Face match
        FaceResult face = callPythonFaceMatch(aadhaar, selfie);
        System.out.println("DEBUG FACE SCORE >>> " + face.score);
        System.out.println("DEBUG FACE HASH  >>> " + face.hash);

        // 5️⃣ Save verification
        OwnerVerification ov = new OwnerVerification();
        ov.setUserId(user.getId());
        ov.setAadhaarImageUrl(aadhaarUrl);
        ov.setSelfieUrl(selfieUrl);
        ov.setOcrText(ocrText);

        ov.setAadhaarNumber(info.number());
        ov.setName(info.name());
        ov.setDob(info.dob());
        ov.setGender(info.gender());

        ov.setFaceScore(face.score);
        ov.setFaceEmbeddingHash(face.hash);

        ov.setLat(lat);
        ov.setLng(lng);
        ov.setAddress(reverseGeocode(lat, lng));

        ov.setStatus(OwnerVerification.Status.PENDING);
        repo.save(ov);
    }

    // =====================================================
    // FETCH LATEST VERIFICATION
    // =====================================================
    public OwnerVerification getVerificationByUserId(Long userId) {
        return repo.findTopByUserIdOrderByIdDesc(userId);
    }

    // =====================================================
    // OCR SERVICE CALL
    // =====================================================
    private String callPythonOCR(MultipartFile aadhaar) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", aadhaar.getResource());

        HttpEntity<MultiValueMap<String, Object>> req =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> res = rest.postForEntity(
                ocrBaseUrl + "/ocr",
                req,
                String.class
        );

        System.out.println("DEBUG OCR RAW RESPONSE >>> " + res.getBody());

        JsonNode json = mapper.readTree(res.getBody());
        return json.path("text").asText("");
    }

    // =====================================================
    // FACE MATCH SERVICE CALL
    // =====================================================
    private FaceResult callPythonFaceMatch(
            MultipartFile aadhaar,
            MultipartFile selfie
    ) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("aadhaar", aadhaar.getResource());
        body.add("selfie", selfie.getResource());

        HttpEntity<MultiValueMap<String, Object>> req =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> res = rest.postForEntity(
                ocrBaseUrl + "/face-match",
                req,
                String.class
        );

        System.out.println("DEBUG FACE RAW RESPONSE >>> " + res.getBody());

        JsonNode json = mapper.readTree(res.getBody());
        return new FaceResult(
                json.path("score").asDouble(),
                json.path("hash").asText()
        );
    }

    // =====================================================
    // REVERSE GEOCODING
    // =====================================================
    private String reverseGeocode(Double lat, Double lng) {
        if (lat == null || lng == null) return null;

        try {
            String url =
                    "https://nominatim.openstreetmap.org/reverse?format=json"
                    + "&lat=" + lat
                    + "&lon=" + lng
                    + "&zoom=18&addressdetails=1";

            ResponseEntity<String> response =
                    rest.getForEntity(url, String.class);

            JsonNode node = mapper.readTree(response.getBody());
            return node.path("display_name").asText(null);

        } catch (Exception e) {
            System.out.println("Reverse Geocode Failed: " + e.getMessage());
            return null;
        }
    }

    // =====================================================
    // INTERNAL RECORD
    // =====================================================
    private record FaceResult(double score, String hash) {}
}
