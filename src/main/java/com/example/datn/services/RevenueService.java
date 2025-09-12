package com.example.datn.services;

import com.example.datn.dto.response.RevenueStatsDto;
import com.example.datn.dto.response.RevenueSummaryDto;
import com.example.datn.repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class RevenueService {
    private final BillRepository billRepository;

    @Autowired
    public RevenueService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    // ===================== SUMMARY (TỔNG QUAN) =====================
    public RevenueSummaryDto getRevenueSummary() {
        RevenueSummaryDto summary = new RevenueSummaryDto();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        // === Hôm nay ===
        BigDecimal todayRevenue = scaleValue(billRepository.getTodayRevenue(startOfToday, endOfToday));
        Long todayOrders = defaultLong(billRepository.getTodayOrderCount(startOfToday, endOfToday));

        summary.setTotalRevenueToday(todayRevenue);
        summary.setTotalOrdersToday(todayOrders);

        // === Toàn hệ thống ===
        BigDecimal totalRevenue = scaleValue(billRepository.getTotalRevenue());
        Long totalCompletedOrders = defaultLong(billRepository.getTotalCompletedOrders());

        summary.setTotalRevenue(totalRevenue);
        summary.setTotalOrders(totalCompletedOrders);

        // === Tháng này ===
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonthTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthTime = today.atTime(LocalTime.MAX);

        BigDecimal monthRevenue = scaleValue(billRepository.getCurrentMonthRevenue(startOfMonthTime, endOfMonthTime));
        Object[] monthRange = billRepository.getRevenueAndOrdersByRange(startOfMonthTime, endOfMonthTime);
        Long monthOrders = extractLong(monthRange, 1);

        summary.setTotalRevenueThisMonth(monthRevenue);
        summary.setTotalOrdersThisMonth(monthOrders);

        // === Năm này ===
        BigDecimal yearRevenue = getCurrentYearRevenue();
        summary.setTotalRevenueThisYear(yearRevenue);
        LocalDate startOfYear = today.withDayOfYear(1);
        Object[] yearRange = billRepository.getRevenueAndOrdersByRange(startOfYear.atStartOfDay(), today.atTime(LocalTime.MAX));
        Long yearOrders = extractLong(yearRange, 1);
        summary.setTotalOrdersThisYear(yearOrders);

        // === Giá trị đơn trung bình (tháng) ===
        if (monthOrders > 0) {
            summary.setAvgOrderValue(monthRevenue.divide(BigDecimal.valueOf(monthOrders), 2, RoundingMode.HALF_UP));
        } else {
            summary.setAvgOrderValue(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        // === % tăng trưởng so với tháng trước ===
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        Object[] lastMonthRange = billRepository.getRevenueAndOrdersByRange(
                startOfLastMonth.atStartOfDay(), endOfLastMonth.atTime(LocalTime.MAX)
        );
        BigDecimal lastMonthRevenue = extractBigDecimal(lastMonthRange, 0);
        BigDecimal growth = BigDecimal.ZERO;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growth = monthRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        summary.setGrowthPercentage(growth);

        // === Dữ liệu cho chart ===
        summary.setDailyStats(getRevenueByDateRange(today.minusDays(30), today));
        summary.setMonthlyStats(getRevenueByMonth(today.getYear()));
        summary.setYearlyStats(getRevenueByYear());

        return summary;
    }

    /** Doanh thu năm hiện tại */
    public BigDecimal getCurrentYearRevenue() {
        return scaleValue(billRepository.getCurrentYearRevenue());
    }

    // ===================== DAILY / MONTHLY / YEARLY =====================
    public List<RevenueStatsDto> getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Object[]> rows = billRepository.getRevenueByDate(start, end);
        List<RevenueStatsDto> out = new ArrayList<>();

        for (Object[] r : rows) {
            LocalDate date = (r[0] instanceof java.sql.Date)
                    ? ((java.sql.Date) r[0]).toLocalDate()
                    : LocalDate.parse(r[0].toString());
            BigDecimal revenue = extractBigDecimal(r, 1);
            Long orders = extractLong(r, 2);

            out.add(new RevenueStatsDto(date, revenue, orders, "day"));
        }
        return out;
    }

    public List<RevenueStatsDto> getRevenueByMonth(int year) {
        List<Object[]> rows = billRepository.getRevenueByMonth(year);
        List<RevenueStatsDto> out = new ArrayList<>();

        for (Object[] r : rows) {
            int y = extractInt(r, 0, year);
            int m = extractInt(r, 1, 0);
            BigDecimal revenue = extractBigDecimal(r, 2);
            Long orders = extractLong(r, 3);

            String period = String.format("%d-%02d", y, m);
            out.add(new RevenueStatsDto(period, revenue, orders));
        }
        return out;
    }

    public List<RevenueStatsDto> getRevenueByYear() {
        List<Object[]> rows = billRepository.getRevenueByYear();
        List<RevenueStatsDto> out = new ArrayList<>();

        for (Object[] r : rows) {
            String y = (r[0] == null) ? "" : r[0].toString();
            BigDecimal revenue = extractBigDecimal(r, 1);
            Long orders = extractLong(r, 2);

            out.add(new RevenueStatsDto(y, revenue, orders));
        }
        return out;
    }

    // ===================== TODAY / TOP PRODUCTS / CUSTOM RANGE =====================
    public BigDecimal getTodayRevenue() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        return scaleValue(billRepository.getTodayRevenue(startOfToday, endOfToday));
    }

    public Long getTodayOrderCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        return defaultLong(billRepository.getTodayOrderCount(startOfToday, endOfToday));
    }

//    public List<Object[]> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
//        return billRepository.getTopSellingProducts(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
//    }

    public Object[] getRevenueByCustomDateRange(LocalDate startDate, LocalDate endDate) {
        Object[] row = billRepository.getRevenueAndOrdersByRange(
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)
        );
        BigDecimal revenue = extractBigDecimal(row, 0);
        Long orders = extractLong(row, 1);
        return new Object[]{revenue, orders};
    }

    // ===================== UTIL METHODS =====================
    private BigDecimal scaleValue(BigDecimal val) {
        return (val == null ? BigDecimal.ZERO : val).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal extractBigDecimal(Object[] arr, int index) {
        if (arr != null && arr.length > index && arr[index] instanceof BigDecimal) {
            return ((BigDecimal) arr[index]).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private Long extractLong(Object[] arr, int index) {
        if (arr != null && arr.length > index && arr[index] instanceof Number) {
            return ((Number) arr[index]).longValue();
        }
        return 0L;
    }

    private int extractInt(Object[] arr, int index, int defaultVal) {
        if (arr != null && arr.length > index && arr[index] instanceof Number) {
            return ((Number) arr[index]).intValue();
        }
        return defaultVal;
    }

    private Long defaultLong(Long val) {
        return val == null ? 0L : val;
    }
}