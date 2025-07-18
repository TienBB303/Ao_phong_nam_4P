package com.example.datn.services;
import com.example.datn.dto.response.ProductStatsDto;
import com.example.datn.dto.response.ProductStatsSummaryDto;
import com.example.datn.repositories.product_and_other.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductStatsService {
    @Autowired
    private ProductRepository productRepository;

    /**
     * Lấy tổng quan thống kê sản phẩm
     */
    public ProductStatsSummaryDto getProductStatsSummary() {
        ProductStatsSummaryDto summary = new ProductStatsSummaryDto();

        try {
            // Tổng quan cơ bản
            Long totalProductsCount = productRepository.countTotalProducts();
            Long activeProductsCount = productRepository.countActiveProducts();
            Long categoriesCount = productRepository.countActiveCategories();
            Long brandsCount = productRepository.countActiveBrands();

            System.out.println("DEBUG Service - Counts:");
            System.out.println("Total Products Count: " + totalProductsCount);
            System.out.println("Active Products Count: " + activeProductsCount);
            System.out.println("Categories Count: " + categoriesCount);
            System.out.println("Brands Count: " + brandsCount);

            summary.setTotalProducts(safeConvertToInteger(totalProductsCount));
            summary.setTotalActiveProducts(safeConvertToInteger(activeProductsCount));
            summary.setTotalCategories(safeConvertToInteger(categoriesCount));
            summary.setTotalBrands(safeConvertToInteger(brandsCount));

            // Thống kê tồn kho
            List<Object[]> stockStats = productRepository.getProductsStockStats();
            long totalStock = 0L;
            System.out.println("DEBUG Service - Stock Stats size: " + stockStats.size());

            for (Object[] row : stockStats) {
                System.out.println("DEBUG Stock row length: " + row.length);
                for (int i = 0; i < row.length; i++) {
                    System.out.println("Index " + i + ": " + row[i]);
                }
                // Index 5 should be totalStock based on query: p.id, p.code, p.name, p.category.name, p.brand.name, SUM(pd.quantity), COUNT(pd.id)
                if (row.length > 5 && row[5] != null) {
                    totalStock += safeConvertToLong(row[5]);
                }
            }
            summary.setTotalStock(totalStock);
            System.out.println("DEBUG Total Stock: " + totalStock);

            // Đếm sản phẩm sắp hết hàng và hết hàng
            List<Object[]> lowStockList = productRepository.getLowStockProducts();
            List<Object[]> outOfStockList = productRepository.getOutOfStockProducts();
            summary.setLowStockCount(lowStockList.size());
            summary.setOutOfStockProducts(outOfStockList.size());

            System.out.println("DEBUG Low stock count: " + lowStockList.size());
            System.out.println("DEBUG Out of stock count: " + outOfStockList.size());

            // Thống kê bán hàng
            List<Object[]> sellingStats = productRepository.getTopSellingProductsByRevenue();
            BigDecimal totalRevenue = BigDecimal.ZERO;
            long totalUnitsSold = 0L;

            System.out.println("DEBUG Selling Stats size: " + sellingStats.size());

            for (Object[] row : sellingStats) {
                System.out.println("DEBUG Selling row length: " + row.length);
                // Index 5 should be totalSold, Index 6 should be totalRevenue
                if (row.length > 6) {
                    if (row[6] != null) {
                        totalRevenue = totalRevenue.add(safeConvertToBigDecimal(row[6]));
                    }
                    if (row[5] != null) {
                        totalUnitsSold += safeConvertToLong(row[5]);
                    }
                }
            }

            summary.setTotalRevenue(totalRevenue);
            summary.setTotalUnitsSold(totalUnitsSold);

            System.out.println("DEBUG Total Revenue: " + totalRevenue);
            System.out.println("DEBUG Total Units Sold: " + totalUnitsSold);

            // Tính giá trị đơn hàng trung bình
            if (totalUnitsSold > 0) {
                summary.setAvgOrderValue(totalRevenue.divide(BigDecimal.valueOf(totalUnitsSold), 2, RoundingMode.HALF_UP));
            } else {
                summary.setAvgOrderValue(BigDecimal.ZERO);
            }

            // Tính tỷ lệ bán hàng trung bình
            if (totalStock > 0) {
                double sellThroughRate = (double) totalUnitsSold / totalStock * 100;
                summary.setAvgSellThroughRate(Math.round(sellThroughRate * 100.0) / 100.0);
            } else {
                summary.setAvgSellThroughRate(0.0);
            }

            // Tìm danh mục và thương hiệu bán chạy nhất
            List<Object[]> categoryStats = productRepository.getCategoryStats();
            if (!categoryStats.isEmpty() && categoryStats.get(0)[0] != null) {
                summary.setTopCategory(categoryStats.get(0)[0].toString());
            }

            List<Object[]> brandStats = productRepository.getBrandStats();
            if (!brandStats.isEmpty() && brandStats.get(0)[0] != null) {
                summary.setTopBrand(brandStats.get(0)[0].toString());
            }

            // Danh sách chi tiết
            summary.setTopSellingProducts(getTopSellingProducts(10));
            summary.setLowStockProducts(getLowStockProductsList(10));
            summary.setCategoryStats(categoryStats);
            summary.setBrandStats(brandStats);

        } catch (Exception e) {
            System.err.println("Error in getProductStatsSummary: " + e.getMessage());
            e.printStackTrace();

            // Return empty summary on error
            summary.setTotalProducts(0);
            summary.setTotalActiveProducts(0);
            summary.setTotalCategories(0);
            summary.setTotalBrands(0);
            summary.setTotalRevenue(BigDecimal.ZERO);
            summary.setTotalUnitsSold(0L);
            summary.setTotalStock(0L);
            summary.setLowStockCount(0);
            summary.setOutOfStockProducts(0);
            summary.setAvgOrderValue(BigDecimal.ZERO);
            summary.setAvgSellThroughRate(0.0);
        }

        return summary;
    }

    /**
     * Lấy danh sách sản phẩm bán chạy
     */
    public List<ProductStatsDto> getTopSellingProducts(int limit) {
        List<Object[]> results = productRepository.getTopSellingProductsByQuantity();
        List<ProductStatsDto> stats = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;

            ProductStatsDto dto = new ProductStatsDto();
            dto.setProductId(safeConvertToInteger(row[0]));
            dto.setProductCode(safeConvertToString(row[1]));
            dto.setProductName(safeConvertToString(row[2]));
            dto.setCategoryName(safeConvertToString(row[3]));
            dto.setBrandName(safeConvertToString(row[4]));
            dto.setTotalSold(safeConvertToLong(row[5]));
            dto.setTotalRevenue(safeConvertToBigDecimal(row[6]));
            dto.setRanking(count + 1);

            // Tính giá trung bình
            if (dto.getTotalSold() > 0) {
                dto.setAvgPrice(dto.getTotalRevenue().divide(BigDecimal.valueOf(dto.getTotalSold()), 2, RoundingMode.HALF_UP));
            } else {
                dto.setAvgPrice(BigDecimal.ZERO);
            }

            stats.add(dto);
            count++;
        }

        return stats;
    }

    /**
     * Lấy danh sách sản phẩm bán chạy theo doanh thu
     */
    public List<ProductStatsDto> getTopSellingProductsByRevenue(int limit) {
        List<Object[]> results = productRepository.getTopSellingProductsByRevenue();
        List<ProductStatsDto> stats = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;

            ProductStatsDto dto = new ProductStatsDto();
            dto.setProductId(safeConvertToInteger(row[0]));
            dto.setProductCode(safeConvertToString(row[1]));
            dto.setProductName(safeConvertToString(row[2]));
            dto.setCategoryName(safeConvertToString(row[3]));
            dto.setBrandName(safeConvertToString(row[4]));
            dto.setTotalSold(safeConvertToLong(row[5]));
            dto.setTotalRevenue(safeConvertToBigDecimal(row[6]));
            dto.setRanking(count + 1);

            if (dto.getTotalSold() > 0) {
                dto.setAvgPrice(dto.getTotalRevenue().divide(BigDecimal.valueOf(dto.getTotalSold()), 2, RoundingMode.HALF_UP));
            } else {
                dto.setAvgPrice(BigDecimal.ZERO);
            }

            stats.add(dto);
            count++;
        }

        return stats;
    }

    /**
     * Lấy danh sách sản phẩm sắp hết hàng
     */
    public List<ProductStatsDto> getLowStockProductsList(int limit) {
        List<Object[]> results = productRepository.getLowStockProducts();
        List<ProductStatsDto> stats = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;

            ProductStatsDto dto = new ProductStatsDto();
            dto.setProductId(safeConvertToInteger(row[0]));
            dto.setProductCode(safeConvertToString(row[1]));
            dto.setProductName(safeConvertToString(row[2]));
            dto.setCategoryName(safeConvertToString(row[3]));
            dto.setBrandName(safeConvertToString(row[4]));
            dto.setTotalStock(safeConvertToLong(row[5]));
            dto.setTotalVariants(safeConvertToLong(row[6]));

            stats.add(dto);
            count++;
        }

        return stats;
    }

    /**
     * Lấy danh sách sản phẩm hết hàng
     */
    public List<ProductStatsDto> getOutOfStockProductsList(int limit) {
        List<Object[]> results = productRepository.getOutOfStockProducts();
        List<ProductStatsDto> stats = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;

            ProductStatsDto dto = new ProductStatsDto();
            dto.setProductId(safeConvertToInteger(row[0]));
            dto.setProductCode(safeConvertToString(row[1]));
            dto.setProductName(safeConvertToString(row[2]));
            dto.setCategoryName(safeConvertToString(row[3]));
            dto.setBrandName(safeConvertToString(row[4]));
            dto.setTotalStock(safeConvertToLong(row[5]));
            dto.setTotalVariants(safeConvertToLong(row[6]));

            stats.add(dto);
            count++;
        }

        return stats;
    }

    /**
     * Lấy thống kê tồn kho chi tiết
     */
    public List<ProductStatsDto> getStockStatsList(int limit) {
        List<Object[]> results = productRepository.getProductsStockStats();
        List<ProductStatsDto> stats = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;

            ProductStatsDto dto = new ProductStatsDto();
            dto.setProductId(safeConvertToInteger(row[0]));
            dto.setProductCode(safeConvertToString(row[1]));
            dto.setProductName(safeConvertToString(row[2]));
            dto.setCategoryName(safeConvertToString(row[3]));
            dto.setBrandName(safeConvertToString(row[4]));
            dto.setTotalStock(safeConvertToLong(row[5]));
            dto.setTotalVariants(safeConvertToLong(row[6]));

            stats.add(dto);
            count++;
        }

        return stats;
    }

    /**
     * Lấy thống kê theo danh mục
     */
    public List<Object[]> getCategoryStats() {
        return productRepository.getCategoryStats();
    }

    /**
     * Lấy thống kê theo thương hiệu
     */
    public List<Object[]> getBrandStats() {
        return productRepository.getBrandStats();
    }

    // ==================== HELPER METHODS ====================

    private Integer safeConvertToInteger(Object value) {
        if (value == null) return 0;
        try {
            if (value instanceof Long) {
                return ((Long) value).intValue();
            }
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Long safeConvertToLong(Object value) {
        if (value == null) return 0L;
        try {
            return Long.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private BigDecimal safeConvertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        try {
            String strValue = value.toString().trim();
            if (strValue.isEmpty() || strValue.equals("null")) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(strValue);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String safeConvertToString(Object value) {
        if (value == null) return "";
        return value.toString().trim();
    }
}
