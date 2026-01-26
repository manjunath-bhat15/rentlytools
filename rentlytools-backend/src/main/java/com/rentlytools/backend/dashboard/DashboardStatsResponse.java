package com.rentlytools.backend.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatsResponse {
    private long activeBookings;
    private double walletBalance;
    private long pendingRequests;
    private long unreadMessages;
}