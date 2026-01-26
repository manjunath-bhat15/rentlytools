package com.rentlytools.backend.verification.owner;

import java.util.regex.*;

public class AadhaarExtractor {

    public static class AadhaarInfo {
        public String number;
        public String name;
        public String dob;
        public String gender;
    }

    public static AadhaarInfo extract(String text) {
        AadhaarInfo info = new AadhaarInfo();

        // Extract Aadhaar Number
        Matcher m = Pattern.compile("\\b\\d{4}\\s\\d{4}\\s\\d{4}\\b").matcher(text);
        if (m.find()) info.number = m.group();

        // Extract DOB
        m = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b").matcher(text);
        if (m.find()) info.dob = m.group();

        // Extract Gender
        if (text.toUpperCase().contains("MALE")) info.gender = "MALE";
        else if (text.toUpperCase().contains("FEMALE")) info.gender = "FEMALE";

        // Extract Name (Simple Heuristic)
        String[] lines = text.split("\n");
        if (lines.length > 2) info.name = lines[1].trim();

        return info;
    }
}
