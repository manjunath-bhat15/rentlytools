package com.rentlytools.backend.core.listing;

import java.util.List;

public record ListingCreateRequest(
        Long ownerId,
        Long categoryId,
        String title,
        String description,
        Double lat,
        Double lng,
        Double pricePerDay,
        Double depositAmount,
        List<String> photos // array of Cloudinary (or other) URLs
) {}