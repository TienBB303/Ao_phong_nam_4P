package com.example.datn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueStatsDto {
    private LocalDate date;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private String period; // "day", "month", "year"

    // Constructor cho thống kê theo tháng/năm
    public RevenueStatsDto(String period, BigDecimal totalRevenue, Long totalOrders) {
        this.period = period;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
    }
}