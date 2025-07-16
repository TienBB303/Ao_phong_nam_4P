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

}
