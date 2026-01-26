package com.rentlytools.backend.verification.owner;

import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrExtractor {

    private static final Pattern AADHAAR_PATTERN =
            Pattern.compile("(\\d{4}\\s\\d{4}\\s\\d{4})");

    private static final Pattern DOB_PATTERN =
            Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");

    private static final Pattern GENDER_PATTERN =
            Pattern.compile("(MALE|FEMALE|Male|Female)");

    public static ExtractedData extract(String text) {
        ExtractedData data = new ExtractedData();

        // Aadhaar
        Matcher m1 = AADHAAR_PATTERN.matcher(text);
        if (m1.find()) data.setAadhaarNumber(m1.group(1));

        // DOB
        Matcher m2 = DOB_PATTERN.matcher(text);
        if (m2.find()) data.setDob(m2.group(1));

        // Gender
        Matcher m3 = GENDER_PATTERN.matcher(text);
        if (m3.find()) data.setGender(m3.group(1).toUpperCase());

        // Name → first line approx
        String name = extractName(text);
        data.setName(name);

        return data;
    }


    private static String extractName(String text) {
        // Rough name extraction from Aadhaar OCR
        String[] parts = text.split(" ");
        if (parts.length > 3) {
            return parts[3] + " " + (parts.length > 4 ? parts[4] : "");
        }
        return null;
    }


    @Data
    public static class ExtractedData {
        private String aadhaarNumber;
        private String name;
        private String dob;
        private String gender;
    }
}