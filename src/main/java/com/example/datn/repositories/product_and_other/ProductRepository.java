package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p")
    List<Product> getAll();

    @Query("SELECT p FROM Product p ORDER BY p.id DESC")
    Page<Product> getAll(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdProduct(Integer id);

    @Query("SELECT MAX(p.code) FROM Product p WHERE p.code LIKE 'SP%' ")
    String findMaxCodeProduct();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name) ")
    Product findNameAlreadyHave(String name);

    @Query("SELECT SUM(p.quantity) FROM ProductDetail p WHERE p.product.id = :productId")
    Integer tongSoLuongTheoSanPham(Integer productId);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :id")
    List<ProductDetail> findAllProductDetail(Integer id);

    @Query("SELECT p FROM Product p " +
            "WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND (:status IS NULL OR p.status = :status)) " +
            "ORDER BY p.id DESC")
    Page<Product> search(String name, Boolean status, Pageable pageable);
}
