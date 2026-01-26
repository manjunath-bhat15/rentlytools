package com.rentlytools.backend.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardStatsController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping("/stats-v2")
    public DashboardStatsResponse getStats(@RequestParam Long userId) {
        return dashboardStatsService.getStats(userId);
    }
}