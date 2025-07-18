package com.example.datn.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatsSummaryDto {
    // Tổng quan
    private Integer totalProducts;           // Tổng số sản phẩm
    private Integer totalActiveProducts;     // Số sản phẩm đang hoạt động
    private Integer totalCategories;         // Tổng số danh mục
    private Integer totalBrands;            // Tổng số thương hiệu

    // Thống kê bán hàng
    private BigDecimal totalRevenue;        // Tổng doanh thu từ sản phẩm
    private Long totalUnitsSold;            // Tổng số lượng đã bán
    private BigDecimal avgOrderValue;       // Giá trị đơn hàng trung bình

    // Thống kê tồn kho
    private Long totalStock;                // Tổng tồn kho
    private Integer lowStockCount;          // Số sản phẩm sắp hết hàng (<10)
    private Integer outOfStockProducts;     // Số sản phẩm hết hàng

    // Thống kê hiệu suất
    private Double avgSellThroughRate;      // Tỷ lệ bán hàng trung bình
    private String topCategory;             // Danh mục bán chạy nhất
    private String topBrand;               // Thương hiệu bán chạy nhất

    // Danh sách chi tiết
    private List<ProductStatsDto> topSellingProducts;    // Top sản phẩm bán chạy
    private List<ProductStatsDto> lowStockProducts;      // Sản phẩm sắp hết hàng
    private List<Object[]> categoryStats;               // Thống kê theo danh mục
    private List<Object[]> brandStats;                  // Thống kê theo thương hiệu
}
