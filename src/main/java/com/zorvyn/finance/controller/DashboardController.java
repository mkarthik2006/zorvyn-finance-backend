package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.ApiResponse;
import com.zorvyn.finance.dto.dashboard.*;
import com.zorvyn.finance.dto.record.FinancialRecordResponse;
import com.zorvyn.finance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
@Tag(name = "Dashboard", description = "Dashboard summary and analytics APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial summary", description = "Returns total income, expense, net balance, and record counts")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary() {
        SummaryResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success("Summary retrieved successfully", summary));
    }

    @GetMapping("/category-wise")
    @Operation(summary = "Get category-wise summary", description = "Returns income/expense breakdown by category")
    public ResponseEntity<ApiResponse<List<CategorySummary>>> getCategoryWiseSummary() {
        List<CategorySummary> summaries = dashboardService.getCategoryWiseSummary();
        return ResponseEntity.ok(ApiResponse.success("Category-wise summary retrieved successfully", summaries));
    }

    @GetMapping("/monthly-trends")
    @Operation(summary = "Get monthly trends", description = "Returns monthly income/expense trends")
    public ResponseEntity<ApiResponse<List<MonthlyTrend>>> getMonthlyTrends() {
        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();
        return ResponseEntity.ok(ApiResponse.success("Monthly trends retrieved successfully", trends));
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get recent activity", description = "Returns the most recent financial records")
    public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        List<FinancialRecordResponse> records = dashboardService.getRecentActivity(limit);
        return ResponseEntity.ok(ApiResponse.success("Recent activity retrieved successfully", records));
    }
}