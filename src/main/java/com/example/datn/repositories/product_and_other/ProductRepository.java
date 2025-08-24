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
            "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND (:status IS NULL OR p.status = :status)) " +
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
    @Query("SELECT p.id, p.code, p.name, c.name, br.name, " +
            "COALESCE(SUM(bd.quantity), 0) AS totalSold, " +
            "COALESCE(SUM(bd.total_price), 0) AS grossRevenue " + // GrossRevenue
            "FROM BillDetails bd " +
            "JOIN bd.productDetail pd " +
            "JOIN pd.product p " +
            "JOIN p.category c " +
            "JOIN p.brand br " +
            "JOIN bd.bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.status = 4 " + // chỉ tính đơn hoàn thành
            "AND p.status = true " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
            "GROUP BY p.id, p.code, p.name, c.name, br.name " +
            "ORDER BY totalSold DESC")
    List<Object[]> getTopSellingProductsByQuantity(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);


    // Sản phẩm bán chạy theo doanh thu (GrossRevenue)- tính theo giá gốc ( dùng cho màn Danh sách sản phẩm bán nhiều nhất)
    //  (gross revenue = chưa trừ phí ship + discount)
    @Query("SELECT p.id, p.code, p.name, c.name, br.name, " +
            "COALESCE(SUM(bd.quantity), 0), " +
            "COALESCE(SUM(bd.total_price), 0) " +
            "FROM BillDetails bd " +
            "JOIN bd.productDetail pd " +
            "JOIN pd.product p " +
            "JOIN p.category c " +
            "JOIN p.brand br " +
            "JOIN bd.bill b " +
            "WHERE b.paymentStatus = true " +
            "AND p.status = true " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
            "GROUP BY p.id, p.code, p.name, c.name, br.name " +
            "ORDER BY SUM(bd.total_price) DESC")
    List<Object[]> getTopSellingProductsByRevenue(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);


    // Thống kê theo danh mục (tính theo giá gốc ( dùng cho màn Danh sách sản phẩm bán nhiều nhất)
    // ProductRepository.java - Cần cập nhật getCategoryStats để lọc theo thời gian
    @Query(value = """
    SELECT c.name, 
           COUNT(p.id) AS productCount,
           COALESCE(SUM(bd.quantity), 0) AS totalSold,
           COALESCE(SUM(bd.total_price), 0) AS grossRevenue,  -- GrossRevenue
           COALESCE(SUM(pd.quantity), 0) AS totalStock
    FROM category c
    LEFT JOIN product p ON p.category_id = c.id AND p.status = 1
    LEFT JOIN product_detail pd ON pd.product_id = p.id
    LEFT JOIN bill_detail bd ON bd.product_detail_id = pd.id
    LEFT JOIN bill b ON bd.bill_id = b.id AND b.payment_status = 1 AND b.status = 4
    WHERE c.status = 1
      AND (:startDate IS NULL OR b.created_at >= :startDate)
      AND (:endDate IS NULL OR b.created_at <= :endDate)
    GROUP BY c.id, c.name
    ORDER BY grossRevenue DESC
""", nativeQuery = true)
    List<Object[]> getCategoryStats(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
    // Thống kê theo thương hiệu (tính theo giá gốc ( dùng cho màn Danh sách sản phẩm bán nhiều nhất)
    @Query(value = """
    SELECT br.name, 
           COUNT(p.id) AS productCount,
           COALESCE(SUM(bd.quantity), 0) AS totalSold,
           COALESCE(SUM(bd.total_price), 0) AS grossRevenue,  -- GrossRevenue
           COALESCE(SUM(pd.quantity), 0) AS totalStock
    FROM brand br
    LEFT JOIN product p ON p.brand_id = br.id AND p.status = 1
    LEFT JOIN product_detail pd ON pd.product_id = p.id
    LEFT JOIN bill_detail bd ON bd.product_detail_id = pd.id
    LEFT JOIN bill b ON bd.bill_id = b.id AND b.payment_status = 1 AND b.status = 4
    WHERE br.status = 1
    GROUP BY br.id, br.name
    ORDER BY grossRevenue DESC
""", nativeQuery = true)
    List<Object[]> getBrandStats();


}
