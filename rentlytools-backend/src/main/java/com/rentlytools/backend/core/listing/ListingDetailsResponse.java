package com.rentlytools.backend.core.listing;

import java.util.List;

public record ListingDetailsResponse(
        Long id,
        Long ownerId,
        Long categoryId,
        String title,
        String description,
        Double lat,
        Double lng,
        Double pricePerDay,
        Double depositAmount,
        Double avgRating,
        String status,
        List<String> photos
) {}
