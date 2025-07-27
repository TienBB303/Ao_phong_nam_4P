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
    @Query("SELECT p.id, p.code, p.name, p.category.name, p.brand.name, " +
            "COALESCE(SUM(bd.quantity), 0) as totalSold, " +
            "COALESCE(SUM(bd.total_price), 0) as totalRevenue " +
            "FROM Product p " +
            "LEFT JOIN ProductDetail pd ON pd.product.id = p.id " +
            "LEFT JOIN BillDetails bd ON bd.productDetail.id = pd.id " +
            "LEFT JOIN Bill b ON bd.bill.id = b.id " +
            "WHERE (b.paymentStatus = true OR b.paymentStatus IS NULL) " +
            "AND p.status = true " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
            "GROUP BY p.id, p.code, p.name, p.category.name, p.brand.name " +
            "ORDER BY COALESCE(SUM(bd.quantity), 0) DESC") // Sắp xếp theo số lượng bán
    List<Object[]> getTopSellingProductsByQuantity(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    // Sản phẩm theo doanh thu
    @Query("SELECT p.id, p.code, p.name, p.category.name, p.brand.name, " +
            "COALESCE(SUM(bd.quantity), 0) as totalSold, " +
            "COALESCE(SUM(bd.total_price), 0) as totalRevenue " +
            "FROM Product p " +
            "LEFT JOIN ProductDetail pd ON pd.product.id = p.id " +
            "LEFT JOIN BillDetails bd ON bd.productDetail.id = pd.id " +
            "LEFT JOIN Bill b ON bd.bill.id = b.id " +
            "WHERE (b.paymentStatus = true OR b.paymentStatus IS NULL) " +
            "AND p.status = true " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
            "GROUP BY p.id, p.code, p.name, p.category.name, p.brand.name " +
            "ORDER BY COALESCE(SUM(bd.total_price), 0) DESC")// Sắp xếp theo doanh thu
    List<Object[]> getTopSellingProductsByRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Thống kê tồn kho
    @Query("SELECT p.id, p.code, p.name, p.category.name, p.brand.name, " +
            "COALESCE(SUM(pd.quantity), 0) as totalStock, " +
            "COUNT(pd.id) as totalVariants " +
            "FROM Product p " +
            "LEFT JOIN ProductDetail pd ON pd.product.id = p.id " +
            "WHERE p.status = true " +
            "GROUP BY p.id, p.code, p.name, p.category.name, p.brand.name " +
            "ORDER BY COALESCE(SUM(pd.quantity), 0) ASC")
    List<Object[]> getProductsStockStats();

    // Sản phẩm sắp hết hàng (< 10)
    @Query("SELECT p.id, p.code, p.name, p.category.name, p.brand.name, " +
            "COALESCE(SUM(pd.quantity), 0) as totalStock, " +
            "COUNT(pd.id) as totalVariants " +
            "FROM Product p " +
            "LEFT JOIN ProductDetail pd ON pd.product.id = p.id " +
            "WHERE p.status = true " +
            "GROUP BY p.id, p.code, p.name, p.category.name, p.brand.name " +
            "HAVING COALESCE(SUM(pd.quantity), 0) < 10 " +
            "ORDER BY COALESCE(SUM(pd.quantity), 0) ASC")
    List<Object[]> getLowStockProducts();

    // Sản phẩm hết hàng
    @Query("SELECT p.id, p.code, p.name, p.category.name, p.brand.name, " +
            "COALESCE(SUM(pd.quantity), 0) as totalStock, " +
            "COUNT(pd.id) as totalVariants " +
            "FROM Product p " +
            "LEFT JOIN ProductDetail pd ON pd.product.id = p.id " +
            "WHERE p.status = true " +
            "GROUP BY p.id, p.code, p.name, p.category.name, p.brand.name " +
            "HAVING COALESCE(SUM(pd.quantity), 0) = 0 " +
            "ORDER BY p.name ASC")
    List<Object[]> getOutOfStockProducts();

    // Thống kê theo danh mục
    // ProductRepository.java - Cần cập nhật getCategoryStats để lọc theo thời gian
    @Query(value = """
    SELECT c.name, COUNT(p.id) as productCount,
    COALESCE(SUM(bd.quantity), 0) as totalSold,
    COALESCE(SUM(bd.total_price), 0) as totalRevenue,
    COALESCE(SUM(pd.quantity), 0) as totalStock
    FROM Category c
    LEFT JOIN Product p ON p.category.id = c.id AND p.status = true
    LEFT JOIN ProductDetail pd ON pd.product.id = p.id
    LEFT JOIN BillDetails bd ON bd.productDetail.id = pd.id
    LEFT JOIN Bill b ON bd.bill.id = b.id AND b.paymentStatus = true
    WHERE c.status = true
      AND (:startDate IS NULL OR b.createdAt >= :startDate)
      AND (:endDate IS NULL OR b.createdAt <= :endDate)
    GROUP BY c.id, c.name
    ORDER BY COALESCE(SUM(bd.total_price), 0) DESC
""", nativeQuery = true)
    List<Object[]> getCategoryStats(@Param("startDate") java.time.LocalDateTime startDate,
                                    @Param("endDate") java.time.LocalDateTime endDate);

    // Thống kê theo thương hiệu
    @Query("SELECT br.name, COUNT(p.id) as productCount, " +
            "COALESCE(SUM(bd.quantity), 0) as totalSold, " +
            "COALESCE(SUM(bd.total_price), 0) as totalRevenue, " +
            "COALESCE(SUM(pd.quantity), 0) as totalStock " +
            "FROM Brand br " +
            "LEFT JOIN Product p ON p.brand.id = br.id AND p.status = true " +
            "LEFT JOIN ProductDetail pd ON pd.product.id = p.id " +
            "LEFT JOIN BillDetails bd ON bd.productDetail.id = pd.id " +
            "LEFT JOIN Bill b ON bd.bill.id = b.id AND b.paymentStatus = true " +
            "WHERE br.status = true " +
            "GROUP BY br.id, br.name " +
            "ORDER BY COALESCE(SUM(bd.total_price), 0) DESC")
    List<Object[]> getBrandStats();

}
