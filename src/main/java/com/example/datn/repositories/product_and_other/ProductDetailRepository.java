package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.entities.product_and_other.Size;
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

//    @Query("select pd from ProductDetail pd where pd.barcode like :barcode and pd.status = true")
    ProductDetail findProductDetailsByBarcode(String barcode);

    @Query("SELECT pd FROM ProductDetail pd " +
            "JOIN FETCH pd.product p " +
            "JOIN FETCH pd.color c " +
            "JOIN FETCH pd.size s " +
            "WHERE pd.quantity > 0 " +
            "AND pd.status = true " +
            "AND ( LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(pd.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    List<ProductDetail> searchProductDetailByKeyword(@Param("keyword") String keyword);

    @Query("SELECT pd FROM ProductDetail pd " +
            "WHERE pd.quantity > 0 " +
            "AND pd.status = true " +
            "AND pd.product.status = true")
    List<ProductDetail> findAllInStock();

    @Query("SELECT pd.product.id, MIN(pd.price), MAX(pd.price) " +
            "FROM ProductDetail pd " +
            "GROUP BY pd.product.id")
    List<Object[]> findMinMaxPricesGroupedByProductId();

    ProductDetail findByProductIdAndColorIdAndSizeId(Integer productId, Integer colorId, Integer sizeId);

    //TienBB
    @Query("SELECT pd.size from ProductDetail pd WHERE pd.product.id = :productId AND pd.status = true AND pd.color.id = :colorId")
    List<Size> findSizesByProductIdAndColor(Integer productId, Integer colorId);
    //TienBB
    @Query("SELECT pd.color from ProductDetail pd WHERE pd.product.id = :productId AND pd.status = true AND pd.size.id = :sizeId")
    List<Color> findColorsByProductIdAndSize(Integer productId, Integer sizeId);
    //TienBB
    @Query("select pd from ProductDetail pd WHERE pd.product.id = :productId")
    List<ProductDetail> findALLProductDetailByProductID(Integer productId);
}
