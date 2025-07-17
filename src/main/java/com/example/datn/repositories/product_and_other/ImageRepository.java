package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Image;
import com.example.datn.entities.product_and_other.Image;
import com.example.datn.entities.product_and_other.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    @Query("SELECT i FROM Image i")
    List<Image> getAll();

    @Query("SELECT i FROM Image i ORDER BY i.id DESC")
    Page<Image> getAll(Pageable pageable);

    @Query("SELECT i FROM Image i WHERE i.id = :id")
    Image findByIdImage(Integer id);

    @Query("SELECT pd FROM ProductDetail pd WHERE pd.product.id = :productId AND pd.color.id = :colorId")
    List<ProductDetail> findByProductIdAndColorId(@Param("productId") Integer productId,
                                                  @Param("colorId") Integer colorId);

    @Query("select i from Image i where i.productDetail.id = :id")
    List<ProductDetail> findAllImageByProductDetailId(Integer id);
}
