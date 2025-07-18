package com.example.datn.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStatsDto {
    private Integer productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private String brandName;

    // Thống kê bán hàng
    private Long totalSold;          // Tổng số lượng đã bán
    private BigDecimal totalRevenue; // Tổng doanh thu
    private BigDecimal avgPrice;     // Giá trung bình

    // Thống kê tồn kho
    private Long totalStock;         // Tổng tồn kho
    private Long totalVariants;      // Tổng số biến thể

    // Thống kê hiệu suất
    private Double sellThroughRate;  // Tỷ lệ bán hàng (sold/stock)
    private Integer ranking;         // Xếp hạng bán chạy

    // Constructor cho sản phẩm bán chạy
    public ProductStatsDto(Integer productId, String productCode, String productName,
                           String categoryName, String brandName, Long totalSold, BigDecimal totalRevenue) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
    }

    // Constructor cho thống kê tồn kho
    public ProductStatsDto(Integer productId, String productCode, String productName,
                           String categoryName, String brandName, Long totalStock, Long totalVariants) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.totalStock = totalStock;
        this.totalVariants = totalVariants;
    }
}
