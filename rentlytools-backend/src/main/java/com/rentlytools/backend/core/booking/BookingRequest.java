package com.rentlytools.backend.core.booking;

public record BookingRequest(
        Long listingId,
        Long renterId,
        String startAt,  // later we convert to LocalDateTime
        String endAt
) {}
