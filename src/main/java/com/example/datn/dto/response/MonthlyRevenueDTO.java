package com.example.datn.dto.response;

import java.math.BigDecimal;

public class MonthlyRevenueDTO {
    private String period;
    private BigDecimal totalRevenue;

    public MonthlyRevenueDTO() {
    }

    public MonthlyRevenueDTO(String period, BigDecimal totalRevenue) {
        this.period = period;
        this.totalRevenue = totalRevenue;
    }

    public String getPeriod() {
        return period;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
