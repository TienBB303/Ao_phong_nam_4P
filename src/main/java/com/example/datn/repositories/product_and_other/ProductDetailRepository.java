package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {

    @Query("select pd from ProductDetail pd")
    List<ProductDetail> getAllProductDetails();

    List<ProductDetail> findByProductIdAndColorId(Integer productId, Integer colorId);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.id = :id")
    ProductDetail findProductDetailById(Integer id);

    @Query("SELECT pd FROM ProductDetail pd " +
            "JOIN FETCH pd.product p " +
            "JOIN FETCH pd.color c " +
            "JOIN FETCH pd.size s " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(pd.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProductDetail> searchProductDetailByKeyword(@Param("keyword") String keyword);

    @Query("SELECT pd.product.id, MIN(pd.price), MAX(pd.price) " +
            "FROM ProductDetail pd " +
            "GROUP BY pd.product.id")
    List<Object[]> findMinMaxPricesGroupedByProductId();

    ProductDetail findByProductIdAndColorIdAndSizeId(Integer productId, Integer colorId, Integer sizeId);

}
