package com.example.datn.controllers.admin;
import com.example.datn.dto.response.ProductStatsDto;
import com.example.datn.dto.response.ProductStatsSummaryDto;
import com.example.datn.services.ProductStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/product-stats")
public class ProductStatsController {
    @Autowired
    private ProductStatsService productStatsService;

    /**
     * Hiển thị trang thống kê sản phẩm
     */
    @GetMapping
    public String productStatsPage(Model model) {
        try {
            ProductStatsSummaryDto summary = productStatsService.getProductStatsSummary();

            // Debug logging
            System.out.println("DEBUG ProductStatsController:");
            System.out.println("Total Products: " + summary.getTotalProducts());
            System.out.println("Active Products: " + summary.getTotalActiveProducts());
            System.out.println("Units Sold: " + summary.getTotalUnitsSold());
            System.out.println("Total Revenue: " + summary.getTotalRevenue());
            System.out.println("Low Stock Count: " + summary.getLowStockCount());
            System.out.println("Out of Stock: " + summary.getOutOfStockProducts());
            System.out.println("Total Stock: " + summary.getTotalStock());

            model.addAttribute("productStatsSummary", summary);
            return "admin/product-stats/product-stats";
        } catch (Exception e) {
            System.err.println("Error in productStatsPage: " + e.getMessage());
            e.printStackTrace();

            // Create empty summary as fallback
            ProductStatsSummaryDto emptySummary = new ProductStatsSummaryDto();
            emptySummary.setTotalProducts(0);
            emptySummary.setTotalActiveProducts(0);
            emptySummary.setTotalUnitsSold(0L);
            emptySummary.setTotalRevenue(java.math.BigDecimal.ZERO);
            emptySummary.setLowStockCount(0);
            emptySummary.setOutOfStockProducts(0);
            emptySummary.setTotalStock(0L);

            model.addAttribute("productStatsSummary", emptySummary);
            return "admin/product-stats/product-stats";
        }
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API: Lấy tổng quan thống kê sản phẩm
     */
    @GetMapping("/api/summary")
    @ResponseBody
    public ResponseEntity<ProductStatsSummaryDto> getProductStatsSummary() {
        ProductStatsSummaryDto summary = productStatsService.getProductStatsSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Test endpoint để debug
     */
    @GetMapping("/api/test")
    @ResponseBody
    public ResponseEntity<String> testEndpoint() {
        try {
            ProductStatsSummaryDto summary = productStatsService.getProductStatsSummary();
            return ResponseEntity.ok("Test OK - Total Products: " + summary.getTotalProducts() +
                    ", Active: " + summary.getTotalActiveProducts() +
                    ", Stock: " + summary.getTotalStock() +
                    ", Revenue: " + summary.getTotalRevenue());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * API: Lấy danh sách sản phẩm bán chạy theo số lượng
     */
    @GetMapping("/api/top-selling")
    @ResponseBody
    public ResponseEntity<List<ProductStatsDto>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {

        List<ProductStatsDto> products = productStatsService.getTopSellingProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * API: Lấy danh sách sản phẩm bán chạy theo doanh thu
     */
    @GetMapping("/api/top-revenue")
    @ResponseBody
    public ResponseEntity<List<ProductStatsDto>> getTopSellingProductsByRevenue(
            @RequestParam(defaultValue = "10") int limit) {

        List<ProductStatsDto> products = productStatsService.getTopSellingProductsByRevenue(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * API: Lấy danh sách sản phẩm sắp hết hàng
     */
    @GetMapping("/api/low-stock")
    @ResponseBody
    public ResponseEntity<List<ProductStatsDto>> getLowStockProducts(
            @RequestParam(defaultValue = "20") int limit) {

        List<ProductStatsDto> products = productStatsService.getLowStockProductsList(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * API: Lấy danh sách sản phẩm hết hàng
     */
    @GetMapping("/api/out-of-stock")
    @ResponseBody
    public ResponseEntity<List<ProductStatsDto>> getOutOfStockProducts(
            @RequestParam(defaultValue = "20") int limit) {

        List<ProductStatsDto> products = productStatsService.getOutOfStockProductsList(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * API: Lấy thống kê tồn kho
     */
    @GetMapping("/api/stock-stats")
    @ResponseBody
    public ResponseEntity<List<ProductStatsDto>> getStockStats(
            @RequestParam(defaultValue = "50") int limit) {

        List<ProductStatsDto> products = productStatsService.getStockStatsList(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * API: Lấy thống kê theo danh mục
     */
    @GetMapping("/api/category-stats")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getCategoryStats() {
        List<Object[]> stats = productStatsService.getCategoryStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * API: Lấy thống kê theo thương hiệu
     */
    @GetMapping("/api/brand-stats")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getBrandStats() {
        List<Object[]> stats = productStatsService.getBrandStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Validate limit parameter
     */
    private boolean isValidLimit(int limit) {
        return limit > 0 && limit <= 100;
    }

}
