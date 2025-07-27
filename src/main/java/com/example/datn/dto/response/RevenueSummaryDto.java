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
    private Long totalOrders; // Tổng số đơn đặt hàng toàn hệ thống
    private BigDecimal totalRevenue;

    private BigDecimal avgOrderValue;
    private BigDecimal growthPercentage;

    private List<RevenueStatsDto> dailyStats;
    private List<RevenueStatsDto> monthlyStats;
    private List<RevenueStatsDto> yearlyStats;

    public Long getTotalOrders() {
        return totalOrders;
    }
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }
}