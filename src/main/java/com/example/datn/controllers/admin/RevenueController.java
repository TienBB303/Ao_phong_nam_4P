package com.example.datn.controllers.admin;
import com.example.datn.dto.response.RevenueStatsDto;
import com.example.datn.dto.response.RevenueSummaryDto;
import com.example.datn.services.RevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {
    @Autowired
    private RevenueService revenueService;

    /**
     * Hiển thị trang thống kê doanh thu
     */
    @GetMapping
    public String revenueStatsPage(Model model) {
        RevenueSummaryDto summary = revenueService.getRevenueSummary();
        model.addAttribute("revenueSummary", summary);
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "admin/revenue/stats";
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy tổng quan doanh thu
     */
    @GetMapping("/api/summary")
    @ResponseBody
    public ResponseEntity<RevenueSummaryDto> getRevenueSummary() {
        RevenueSummaryDto summary = revenueService.getRevenueSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * API: Thống kê doanh thu theo khoảng ngày
     */
    @GetMapping("/api/daily")
    @ResponseBody
    public ResponseEntity<List<RevenueStatsDto>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<RevenueStatsDto> stats = revenueService.getRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Thống kê doanh thu theo tháng trong năm
     */
    @GetMapping("/api/monthly")
    @ResponseBody
    public ResponseEntity<List<RevenueStatsDto>> getMonthlyRevenue(
            @RequestParam(defaultValue = "2024") int year) {

        List<RevenueStatsDto> stats = revenueService.getRevenueByMonth(year);
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Thống kê doanh thu theo năm
     */
    @GetMapping("/api/yearly")
    @ResponseBody
    public ResponseEntity<List<RevenueStatsDto>> getYearlyRevenue() {
        List<RevenueStatsDto> stats = revenueService.getRevenueByYear();
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Lấy thống kê hôm nay
     */
    @GetMapping("/api/today")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("revenue", revenueService.getTodayRevenue());
        stats.put("orders", revenueService.getTodayOrderCount());
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Lấy sản phẩm bán chạy
     */
    @GetMapping("/api/top-products")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getTopSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {

        List<Object[]> products = revenueService.getTopSellingProducts(startDate, endDate);

        // Giới hạn số lượng sản phẩm trả về
        if (products.size() > limit) {
            products = products.subList(0, limit);
        }

        return ResponseEntity.ok(products);
    }

    /**
     * API: Thống kê doanh thu theo khoảng thời gian tùy chỉnh
     */
    @GetMapping("/api/custom-range")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomRangeRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Object[] result = revenueService.getRevenueByCustomDateRange(startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", result[0]);
        response.put("totalOrders", result[1]);
        response.put("startDate", startDate);
        response.put("endDate", endDate);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Lấy thống kê chi tiết theo ngày trong tháng
     */
    @GetMapping("/api/month-detail")
    @ResponseBody
    public ResponseEntity<List<RevenueStatsDto>> getMonthDetailRevenue(
            @RequestParam(defaultValue = "2024") int year,
            @RequestParam(defaultValue = "1") int month) {

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<RevenueStatsDto> stats = revenueService.getRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * Validate date range
     */
    private boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }
}
