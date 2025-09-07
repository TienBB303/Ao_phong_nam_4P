package com.example.datn.services;
import com.example.datn.dto.response.ProductStatsDto;
import com.example.datn.dto.response.ProductStatsSummaryDto;
import com.example.datn.repositories.product_and_other.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime; // Thêm import này
import com.example.datn.repositories.BillRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductStatsService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BillRepository billRepository;

    /**
     * Lấy tổng quan thống kê sản phẩm
     */
    public ProductStatsSummaryDto getProductStatsSummary(LocalDateTime startDate, LocalDateTime endDate) {
        ProductStatsSummaryDto summary = new ProductStatsSummaryDto();

        try {
            // Tổng quan cơ bản
            Long totalProductsCount = productRepository.countActiveProducts();
            Long categoriesCount = productRepository.countActiveCategories();
            Long brandsCount = productRepository.countActiveBrands();

            System.out.println("DEBUG Service - Counts:");
            System.out.println("Total Products Count (Active): " + totalProductsCount);
            System.out.println("Categories Count: " + categoriesCount);
            System.out.println("Brands Count: " + brandsCount);

            summary.setTotalProducts(safeConvertToInteger(totalProductsCount));       // Tổng sản phẩm = sp đang hoạt động
            summary.setTotalActiveProducts(safeConvertToInteger(totalProductsCount));
            summary.setTotalCategories(safeConvertToInteger(categoriesCount));
            summary.setTotalBrands(safeConvertToInteger(brandsCount));

//            // Thống kê tồn kho
//            List<Object[]> stockStats = productRepository.getProductsStockStats();
//            long totalStock = 0L;
//            System.out.println("DEBUG Service - Stock Stats size: " + stockStats.size());
//
//            for (Object[] row : stockStats) {
//                System.out.println("DEBUG Stock row length: " + row.length);
//                for (int i = 0; i < row.length; i++) {
//                    System.out.println("Index " + i + ": " + row[i]);
//                }
//                // Index 5 should be totalStock based on query: p.id, p.code, p.name, p.category.name, p.brand.name, SUM(pd.quantity), COUNT(pd.id)
//                if (row.length > 5 && row[5] != null) {
//                    totalStock += safeConvertToLong(row[5]);
//                }
//            }
//            summary.setTotalStock(totalStock);
//            System.out.println("DEBUG Total Stock: " + totalStock);


            // Thống kê bán hàng
            List<Object[]> sellingStats = productRepository.getTopSellingProductsByRevenue(startDate, endDate); // Lấy doanh thu tổng quan
            BigDecimal totalRevenue = billRepository.getTotalRevenue(); // đã trừ ship + discount
            Long totalUnitsSold = billRepository.getTotalProductsSold();//tổng số lượng bán

//            System.out.println("DEBUG Selling Stats size: " + sellingStats.size());

//            for (Object[] row : sellingStats) {
//                System.out.println("DEBUG Selling row length: " + row.length);
//                // Index 5 should be totalSold, Index 6 should be totalRevenue
//                if (row.length > 6) {
//                    if (row[6] != null) {
//                        totalRevenue = totalRevenue.add(safeConvertToBigDecimal(row[6]));
//                    }
//                    if (row[5] != null) {
//                        totalUnitsSold += safeConvertToLong(row[5]);
//                    }
//                }
//            }

            summary.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            summary.setTotalUnitsSold(totalUnitsSold != null ? totalUnitsSold : 0L);

//            System.out.println("DEBUG Total Revenue: " + totalRevenue);
//            System.out.println("DEBUG Total Units Sold: " + totalUnitsSold);

            // Tính giá trị đơn hàng trung bình
            if (summary.getTotalUnitsSold() > 0) {
                summary.setAvgOrderValue(summary.getTotalRevenue()
                        .divide(BigDecimal.valueOf(summary.getTotalUnitsSold()), 2, RoundingMode.HALF_UP));
            } else {
                summary.setAvgOrderValue(BigDecimal.ZERO);
            }

            // Tìm danh mục và thương hiệu bán chạy nhất
            List<Object[]> categoryStatsSummary = productRepository.getCategoryStats(startDate, endDate);
            if (!categoryStatsSummary.isEmpty() && categoryStatsSummary.get(0)[0] != null) {
                summary.setTopCategory(categoryStatsSummary.get(0)[0].toString());
            }

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
    public List<ProductStatsDto> getTopSellingProducts(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = productRepository.getTopSellingProductsByQuantity(startDate, endDate); // Truyền startDate và endDate
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
    public List<ProductStatsDto> getTopSellingProductsByRevenue(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = productRepository.getTopSellingProductsByRevenue(startDate, endDate); // Truyền startDate và endDate
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
     * Lấy thống kê theo danh mục
     */
    // ProductStatsService.java - Cần cập nhật
    public List<Object[]> getCategoryStats(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return productRepository.getCategoryStats(startDate, endDate);
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


    /**
     * Trả về danh sách sản phẩm bán chạy theo số lượng (label-value)
     */
    public List<Map<String, Object>> getTopSellingProductsLabel(int limit, LocalDateTime startDate, LocalDateTime endDate) {
        List<ProductStatsDto> list = getTopSellingProducts(limit, startDate, endDate);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductStatsDto dto : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", dto.getProductName());
            map.put("value", dto.getTotalSold());
            result.add(map);
        }
        return result;
    }

    /**
     * Trả về thống kê doanh thu theo danh mục (label-value)
     */
    public List<Map<String, Object>> getCategoryStatsLabel(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> stats = getCategoryStats(startDate, endDate);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", row[0]); // Tên danh mục
            map.put("value", row[3]); // ✅ grossRevenue (doanh thu)
            result.add(map);
        }
        return result;
    }
}
