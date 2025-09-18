package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.entities.product_and_other.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p")
    List<Product> getAll();

    @Query("SELECT p FROM Product p ORDER BY p.id DESC")
    Page<Product> getAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdProduct(Integer id);

    @Query("SELECT p FROM Product p WHERE LOWER(p.code)  = LOWER(:code)")
    Product findByCodeProduct(String code);

    @Query("SELECT MAX(p.code) FROM Product p WHERE p.code LIKE 'SP%' ")
    String findMaxCodeProduct();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name) ")
    Product findNameAlreadyHave(String name);

    @Query("SELECT SUM(p.quantity) FROM ProductDetail p WHERE p.product.id = :productId")
    Integer tongSoLuongTheoSanPham(Integer productId);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :id")
    List<ProductDetail> findAllProductDetail(Integer id);
    @Query("SELECT DISTINCT pd.color FROM ProductDetail pd WHERE pd.product.id = :productId")
    List<Color> findColorsByProductId(@Param("productId") Integer productId);

    @Query("SELECT DISTINCT pd.size FROM ProductDetail pd WHERE pd.product.id = :productId")
    List<Size> findSizesByProductId(@Param("productId") Integer productId);

    Product findByCode(String code);

    @Query("SELECT p FROM Product p " +
            "WHERE ( (LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "   OR  (LOWER(p.code) LIKE LOWER(CONCAT('%', :name, '%'))) ) " +   // <- gom OR lại
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:materialId IS NULL OR p.material.id = :materialId) " +
            "ORDER BY p.id DESC")
    Page<Product> search(String name,
                         Boolean status,
                         @Param("categoryId") Integer categoryId,
                         @Param("brandId") Integer brandId,
                         @Param("materialId") Integer materialId,
                         Pageable pageable);

    //Khanh: tim kiem san pham tren web
    @Query("SELECT p FROM Product p " +
            "JOIN p.brand b " +
            "JOIN p.category c " +
            "JOIN p.material m " +
            "WHERE p.status = true " +
            "AND b.status = true " +
            "AND c.status = true " +
            "AND m.status = true " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:materialId IS NULL OR p.material.id = :materialId) " +
            "ORDER BY p.id DESC")
    Page<Product> searchAllFields(
            @Param("name") String name,
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("materialId") Integer materialId,
            Pageable pageable);
// ==================== THỐNG KÊ SẢN PHẨM ====================

    // Đếm tổng số sản phẩm
    @Query(value = "SELECT COUNT(id) FROM product", nativeQuery = true)
    Long countTotalProducts();

    // Đếm sản phẩm đang hoạt động
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = true")
    Long countActiveProducts();

    // Đếm số danh mục
    @Query("SELECT COUNT(DISTINCT p.category.id) FROM Product p WHERE p.category.status = true")
    Long countActiveCategories();

    // Đếm số thương hiệu
    @Query("SELECT COUNT(DISTINCT p.brand.id) FROM Product p WHERE p.brand.status = true")
    Long countActiveBrands();
    // Sản phẩm bán chạy nhất theo số lượng (Sử dụng cho bảng chính)
    @Query(value = """
    SELECT p.id, p.code, p.name, c.name AS category_name, br.name AS brand_name,
           COALESCE(SUM(bd.quantity), 0) AS totalSold,
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 AND b.payment_status = 1 THEN
                       (
                           (CAST(COALESCE(bd.total_price, 0) AS DECIMAL(38, 10))
                            / NULLIF(CAST(COALESCE(bsum.total_price_sum, 0) AS DECIMAL(38, 10)), 0))
                           *
                           (CAST(COALESCE(b.total_amount, 0) AS DECIMAL(38, 2))
                            - CAST(COALESCE(b.discount_amount, 0) AS DECIMAL(38, 2)))
                       )
                   ELSE 0
               END
           ), 0) AS netRevenue
    FROM bill_detail bd
    JOIN bill b ON b.id = bd.bill_id
    JOIN product_detail pd ON pd.id = bd.product_detail_id
    JOIN product p ON p.id = pd.product_id
    JOIN category c ON c.id = p.category_id
    JOIN brand br ON br.id = p.brand_id
    LEFT JOIN (
        SELECT bd2.bill_id, SUM(COALESCE(bd2.total_price, 0)) AS total_price_sum
        FROM bill_detail bd2
        GROUP BY bd2.bill_id
    ) bsum ON bsum.bill_id = b.id
    WHERE b.payment_status = 1
      AND b.status = 4
      AND p.status = 1
      AND (:startDate IS NULL OR b.created_at >= :startDate)
      AND (:endDate IS NULL OR b.created_at <= :endDate)
    GROUP BY p.id, p.code, p.name, c.name, br.name
    ORDER BY totalSold DESC
    """, nativeQuery = true)
    List<Object[]> getTopSellingProductsByQuantity(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);


    // Sản phẩm bán chạy theo doanh thu (net revenue = total-checkout)
    @Query(value = """
    SELECT p.id, p.code, p.name, c.name AS category_name, br.name AS brand_name,
           COALESCE(SUM(bd.quantity), 0) AS totalSold,
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 AND b.payment_status = 1 THEN
                       (
                           (CAST(COALESCE(bd.total_price, 0) AS DECIMAL(38, 10))
                            / NULLIF(CAST(COALESCE(bsum.total_price_sum, 0) AS DECIMAL(38, 10)), 0))
                           *
                           (CAST(COALESCE(b.total_amount, 0) AS DECIMAL(38, 2))
                            - CAST(COALESCE(b.discount_amount, 0) AS DECIMAL(38, 2)))
                       )
                   ELSE 0
               END
           ), 0) AS netRevenue
    FROM bill_detail bd
    JOIN bill b ON b.id = bd.bill_id
    JOIN product_detail pd ON pd.id = bd.product_detail_id
    JOIN product p ON p.id = pd.product_id
    JOIN category c ON c.id = p.category_id
    JOIN brand br ON br.id = p.brand_id
    LEFT JOIN (
        SELECT bd2.bill_id, SUM(COALESCE(bd2.total_price, 0)) AS total_price_sum
        FROM bill_detail bd2
        GROUP BY bd2.bill_id
    ) bsum ON bsum.bill_id = b.id
    WHERE b.payment_status = 1
      AND b.status = 4
      AND p.status = 1
      AND (:startDate IS NULL OR b.created_at >= :startDate)
      AND (:endDate IS NULL OR b.created_at <= :endDate)
    GROUP BY p.id, p.code, p.name, c.name, br.name
    ORDER BY netRevenue DESC
    """, nativeQuery = true)
    List<Object[]> getTopSellingProductsByRevenue(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);


    // Thống kê theo danh mục (tính theo giá gốc ( dùng cho màn Danh sách sản phẩm bán nhiều nhất)
    // ProductRepository.java - Cần cập nhật getCategoryStats để lọc theo thời gian
    @Query(value = """
    SELECT c.name, 
           COUNT(DISTINCT p.id) AS productCount,
           COALESCE(SUM(bd.quantity), 0) AS totalSold,
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 AND b.payment_status = 1 THEN
                       (
                           (CAST(COALESCE(bd.total_price, 0) AS DECIMAL(38, 10))
                            / NULLIF(CAST(COALESCE(bsum.total_price_sum, 0) AS DECIMAL(38, 10)), 0))
                           *
                           (CAST(COALESCE(b.total_amount, 0) AS DECIMAL(38, 2))
                            - CAST(COALESCE(b.discount_amount, 0) AS DECIMAL(38, 2)))
                       )
                   ELSE 0
               END
           ), 0) AS netRevenue,
           COALESCE(SUM(pd.quantity), 0) AS totalStock
    FROM category c
    LEFT JOIN product p 
           ON p.category_id = c.id AND p.status = 1
    LEFT JOIN product_detail pd 
           ON pd.product_id = p.id
    LEFT JOIN bill_detail bd 
           ON bd.product_detail_id = pd.id
    LEFT JOIN bill b 
           ON bd.bill_id = b.id
          AND b.payment_status = 1
          AND b.status = 4
          AND (:startDate IS NULL OR b.created_at >= :startDate)
          AND (:endDate IS NULL OR b.created_at <= :endDate)
    LEFT JOIN (
        SELECT bd2.bill_id, SUM(COALESCE(bd2.total_price, 0)) AS total_price_sum
        FROM bill_detail bd2
        GROUP BY bd2.bill_id
    ) bsum ON bsum.bill_id = b.id       
    WHERE c.status = 1
    GROUP BY c.id, c.name
    ORDER BY netRevenue DESC
    """, nativeQuery = true)
    List<Object[]> getCategoryStats(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
    // Thống kê theo thương hiệu (tính theo giá gốc ( dùng cho màn Danh sách sản phẩm bán nhiều nhất)
    @Query(value = """
        SELECT
            br.name,
            COUNT(p.id) AS productCount,
            COALESCE(SUM(bd.quantity), 0) AS totalSold,
            COALESCE(SUM(
                CASE
                    WHEN b.payment_status = 1 AND b.status = 4 THEN
                        (
                            (CAST(COALESCE(bd.total_price, 0) AS DECIMAL(38, 10))
                             / NULLIF(CAST(SUM(COALESCE(bd.total_price, 0)) OVER (PARTITION BY b.id) AS DECIMAL(38, 10)), 0))
                            *
                            (CAST(COALESCE(b.total_amount, 0) AS DECIMAL(38, 2))
                             - CAST(COALESCE(b.discount_amount, 0) AS DECIMAL(38, 2)))
                        )
                    ELSE 0
                END
            ), 0) AS netRevenue,
            COALESCE(SUM(pd.quantity), 0) AS totalStock
        FROM brand br
        LEFT JOIN product p
               ON p.brand_id = br.id AND p.status = 1
        LEFT JOIN product_detail pd
               ON pd.product_id = p.id
        LEFT JOIN bill_detail bd
               ON bd.product_detail_id = pd.id
        LEFT JOIN bill b
               ON bd.bill_id = b.id
              AND b.payment_status = 1
              AND b.status = 4
        WHERE br.status = 1
        GROUP BY br.id, br.name
        ORDER BY netRevenue DESC
        """, nativeQuery = true)
    List<Object[]> getBrandStats();


}
