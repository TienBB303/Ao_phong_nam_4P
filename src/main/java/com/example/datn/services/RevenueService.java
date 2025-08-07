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
    @Autowired
    private BillRepository billRepository;
    /**
     * Lấy thống kê doanh thu theo khoảng ngày
     */
    public List<RevenueStatsDto> getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = billRepository.getRevenueByDateRange(startDateTime, endDateTime);
        List<RevenueStatsDto> stats = new ArrayList<>();

        for (Object[] row : results) {
            LocalDate date = null;
            BigDecimal revenue = BigDecimal.ZERO;
            Long orders = 0L;


            if (row[0] != null) {
                try {
                    if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else if (row[0] instanceof java.time.LocalDate) {
                        date = (LocalDate) row[0];
                    } else {
                        String dateStr = row[0].toString().trim();
                        if (!dateStr.isEmpty() && !dateStr.equals("null")) {
                            date = LocalDate.parse(dateStr);
                        }
                    }
                } catch (Exception e) {
                    date = LocalDate.now(); // fallback to today
                }
            }

            // Safe casting for revenue
            if (row[1] != null) {
                try {
                    String revenueStr = row[1].toString().trim();
                    if (!revenueStr.isEmpty() && !revenueStr.equals("null")) {
                        revenue = new BigDecimal(revenueStr);
                    }
                } catch (NumberFormatException e) {
                    revenue = BigDecimal.ZERO;
                }
            }

            // Safe casting for orders
            if (row[2] != null) {
                try {
                    String ordersStr = row[2].toString().trim();
                    if (!ordersStr.isEmpty() && !ordersStr.equals("null")) {
                        orders = Long.valueOf(ordersStr);
                    }
                } catch (NumberFormatException e) {
                    orders = 0L;
                }
            }

            stats.add(new RevenueStatsDto(date, revenue, orders, "day"));
        }

        return stats;
    }

    /**
     * Lấy thống kê doanh thu theo tháng trong năm
     */
    public List<RevenueStatsDto> getRevenueByMonth(int year) {
        List<Object[]> results = billRepository.getRevenueByMonth(year);
        List<RevenueStatsDto> stats = new ArrayList<>();

        for (Object[] row : results) {
            Integer yearResult = 0;
            Integer month = 0;
            BigDecimal revenue = BigDecimal.ZERO;
            Long orders = 0L;

            // Safe casting
            if (row[0] != null) {
                try {
                    yearResult = Integer.valueOf(row[0].toString().trim());
                } catch (NumberFormatException e) {
                    yearResult = 0;
                }
            }
            if (row[1] != null) {
                try {
                    month = Integer.valueOf(row[1].toString().trim());
                } catch (NumberFormatException e) {
                    month = 0;
                }
            }
            if (row[2] != null) {
                try {
                    String revenueStr = row[2].toString().trim();
                    if (!revenueStr.isEmpty() && !revenueStr.equals("null")) {
                        revenue = new BigDecimal(revenueStr);
                    }
                } catch (NumberFormatException e) {
                    revenue = BigDecimal.ZERO;
                }
            }
            if (row[3] != null) {
                try {
                    String ordersStr = row[3].toString().trim();
                    if (!ordersStr.isEmpty() && !ordersStr.equals("null")) {
                        orders = Long.valueOf(ordersStr);
                    }
                } catch (NumberFormatException e) {
                    orders = 0L;
                }
            }

            String period = String.format("%d-%02d", yearResult, month);
            stats.add(new RevenueStatsDto(period, revenue, orders));
        }

        return stats;
    }

    /**
     * Lấy thống kê doanh thu theo năm
     */
    public List<RevenueStatsDto> getRevenueByYear() {
        List<Object[]> results = billRepository.getRevenueByYear();
        List<RevenueStatsDto> stats = new ArrayList<>();

        for (Object[] row : results) {
            Integer year = 0;
            BigDecimal revenue = BigDecimal.ZERO;
            Long orders = 0L;

            if (row[0] != null) {
                try {
                    year = Integer.valueOf(row[0].toString().trim());
                } catch (NumberFormatException e) {
                    year = 0;
                }
            }
            if (row[1] != null) {
                try {
                    String revenueStr = row[1].toString().trim();
                    if (!revenueStr.isEmpty() && !revenueStr.equals("null")) {
                        revenue = new BigDecimal(revenueStr);
                    }
                } catch (NumberFormatException e) {
                    revenue = BigDecimal.ZERO;
                }
            }
            if (row[2] != null) {
                try {
                    String ordersStr = row[2].toString().trim();
                    if (!ordersStr.isEmpty() && !ordersStr.equals("null")) {
                        orders = Long.valueOf(ordersStr);
                    }
                } catch (NumberFormatException e) {
                    orders = 0L;
                }
            }

            stats.add(new RevenueStatsDto(year.toString(), revenue, orders));
        }

        return stats;
    }

    /**
     * Lấy tổng quan doanh thu
     */
    public RevenueSummaryDto getRevenueSummary() {
        RevenueSummaryDto summary = new RevenueSummaryDto();

        // Thống kê hôm nay
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        // Doanh thu hôm nay chỉ lấy bill hoàn thành
        BigDecimal todayRevenue = billRepository.getSimpleRevenueSumWithStatus(startOfToday, endOfToday, 4);
        Long todayOrders = billRepository.getSimpleOrderCountWithStatus(startOfToday, endOfToday, 4);
        summary.setTotalRevenueToday(todayRevenue);
        summary.setTotalOrdersToday(todayOrders);

        // Tổng số đơn đặt hàng toàn hệ thống (chỉ lấy bill hoàn thành)
        summary.setTotalOrders(billRepository.countByStatusAndPaid(4,1));

        // Tổng doanh thu toàn hệ thống (không lọc thời gian, chỉ bill hoàn thành)
        summary.setTotalRevenue(billRepository.getTotalRevenueCompleted());

        // Thống kê tháng này
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthDateTime = today.atTime(LocalTime.MAX);
        BigDecimal monthRevenue = billRepository.getSimpleRevenueSumWithStatus(startOfMonthDateTime, endOfMonthDateTime, 4);
        Long monthOrders = billRepository.getSimpleOrderCountWithStatus(startOfMonthDateTime, endOfMonthDateTime, 4);
        if (monthRevenue == null) monthRevenue = BigDecimal.ZERO;
        if (monthOrders == null) monthOrders = 0L;
        summary.setTotalRevenueThisMonth(monthRevenue);
        summary.setTotalOrdersThisMonth(monthOrders);

        // Thống kê năm này
        LocalDate startOfYear = today.withDayOfYear(1);
        LocalDateTime startOfYearDateTime = startOfYear.atStartOfDay();
        LocalDateTime endOfYearDateTime = today.atTime(LocalTime.MAX);
        // Tổng đơn hàng từ đầu năm (cho thống kê có lọc)
        BigDecimal yearRevenue = billRepository.getSimpleRevenueSumWithStatus(startOfYearDateTime, endOfYearDateTime, 4);
        Long yearOrders = billRepository.getSimpleOrderCountWithStatus(startOfYearDateTime, endOfYearDateTime, 4);
        if (yearRevenue == null) yearRevenue = BigDecimal.ZERO;
        if (yearOrders == null) yearOrders = 0L;
        summary.setTotalRevenueThisYear(yearRevenue);
        summary.setTotalOrdersThisYear(yearOrders);

        // Tính giá trị đơn hàng trung bình
        if (summary.getTotalOrdersThisMonth() > 0) {
            summary.setAvgOrderValue(
                    summary.getTotalRevenueThisMonth()
                            .divide(BigDecimal.valueOf(summary.getTotalOrdersThisMonth()), 2, RoundingMode.HALF_UP)
            );
        } else {
            summary.setAvgOrderValue(BigDecimal.ZERO);
        }

        // Tính % tăng trưởng so với tháng trước
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        BigDecimal lastMonthRevenue = billRepository.getSimpleRevenueSumWithStatus(
                startOfLastMonth.atStartOfDay(),
                endOfLastMonth.atTime(LocalTime.MAX),
                4  // chỉ lấy đơn hoàn thành
        );

        if (lastMonthRevenue == null) lastMonthRevenue = BigDecimal.ZERO;

        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = summary.getTotalRevenueThisMonth()
                    .subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            summary.setGrowthPercentage(growth);
        } else {
            summary.setGrowthPercentage(BigDecimal.ZERO);
        }

        // Thêm thống kê chi tiết
        summary.setDailyStats(getRevenueByDateRange(today.minusDays(30), today));
        summary.setMonthlyStats(getRevenueByMonth(today.getYear()));
        summary.setYearlyStats(getRevenueByYear());

        return summary;
    }

    /**
     * Lấy doanh thu hôm nay
     */
    public BigDecimal getTodayRevenue() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        BigDecimal revenue = billRepository.getSimpleRevenueSumWithStatus(startOfToday, endOfToday, 4);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Lấy số đơn hàng hôm nay
     */
    public Long getTodayOrderCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        Long count = billRepository.getTodayOrderCount(startOfToday, endOfToday);
        return count != null ? count : 0L;
    }

    /**
     * Lấy thống kê sản phẩm bán chạy
     */
    public List<Object[]> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return billRepository.getTopSellingProducts(startDateTime, endDateTime);
    }

    /**
     * Lấy doanh thu theo khoảng thời gian tùy chỉnh
     */
    public Object[] getRevenueByCustomDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        BigDecimal revenue = billRepository.getSimpleRevenueSum(startDateTime, endDateTime);
        Long orders = billRepository.getSimpleOrderCount(startDateTime, endDateTime);

        if (revenue == null) revenue = BigDecimal.ZERO;
        if (orders == null) orders = 0L;

        return new Object[]{revenue, orders};
    }
}