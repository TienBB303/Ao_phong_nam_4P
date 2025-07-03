package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {

    List<ProductDetail> findByProductIdAndColorId(Integer productId, Integer colorId);
}
