package com.rentlytools.backend.core.booking;

public record BookingCreateRequest(
        Long listingId,
        Long renterId,
        String startAt, // iso 2025-11-12T10:00:00
        String endAt
) {}
