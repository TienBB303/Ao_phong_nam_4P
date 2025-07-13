package com.example.datn.repositories.cart;

import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartDetailRepositoty extends JpaRepository<CartDetail,Integer> {

    @Query("select cd from CartDetail cd where cd.id = :id")
    CartDetail findCartDetailById(Integer id);

    @Query("select cd from CartDetail  cd where cd.cart.id = :cartId And cd.productDetail.id = :productDetailId")
    CartDetail findByCartAndProductDetailId(Integer cartId, Integer productDetailId);

    @Query("select count(cd) from CartDetail cd where cd.cart.id = :id")
    Integer countItemInCartByCartId(Integer id);

    @Query("select sum(cd.quantity) from CartDetail cd where cd.cart.id = :id")
    Integer countAllItemInCartByCartId(Integer id);

}
