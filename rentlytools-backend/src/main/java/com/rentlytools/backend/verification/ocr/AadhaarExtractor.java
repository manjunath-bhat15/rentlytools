package com.rentlytools.backend.verification.ocr;

import java.util.regex.*;

public class AadhaarExtractor {

    public static AadhaarInfo extract(String text) {

        // Aadhaar Number
        Matcher aadhaarMatcher = Pattern.compile("(\\d{4}\\s?\\d{4}\\s?\\d{4})")
                .matcher(text);
        String aadhaar = aadhaarMatcher.find()
                ? aadhaarMatcher.group(1).replaceAll("\\s", "")
                : null;

        // Name (first + middle + last)
        Matcher nameMatcher = Pattern.compile("([A-Z][a-zA-Z]+(?: [A-Z][a-zA-Z]+)+)")
                .matcher(text);
        String name = nameMatcher.find() ? nameMatcher.group(1) : null;

        // DOB
        Matcher dobMatcher = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})")
                .matcher(text);
        String dob = dobMatcher.find() ? dobMatcher.group(1) : null;

        // Gender
        String gender = null;
        if (text.toUpperCase().contains("MALE")) gender = "MALE";
        if (text.toUpperCase().contains("FEMALE")) gender = "FEMALE";

        return new AadhaarInfo(aadhaar, name, dob, gender);
    }

    public record AadhaarInfo(String number, String name, String dob, String gender) { }
}