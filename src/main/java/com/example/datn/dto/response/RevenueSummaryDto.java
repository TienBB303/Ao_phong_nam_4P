package com.example.datn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueSummaryDto {
    private BigDecimal totalRevenueToday;
    private BigDecimal totalRevenueThisMonth;
    private BigDecimal totalRevenueThisYear;

    private Long totalOrdersToday;
    private Long totalOrdersThisMonth;
    private Long totalOrdersThisYear;

    private BigDecimal avgOrderValue;
    private BigDecimal growthPercentage;

    private List<RevenueStatsDto> dailyStats;
    private List<RevenueStatsDto> monthlyStats;
    private List<RevenueStatsDto> yearlyStats;
}