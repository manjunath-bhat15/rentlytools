package com.rentlytools.backend.dashboard;

public record DashboardStats(
        long activeBookings,
        double walletBalance,
        long pendingRequests,
        long unreadMessages
) {}