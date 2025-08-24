package com.example.datn.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
public class DailyRevenueDTO {
    // Format JSON cho đẹp: "yyyy-MM-dd"
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private BigDecimal totalRevenue;

    public DailyRevenueDTO() {
    }

    public DailyRevenueDTO(LocalDate date, BigDecimal totalRevenue) {
        this.date = date;
        this.totalRevenue = totalRevenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
