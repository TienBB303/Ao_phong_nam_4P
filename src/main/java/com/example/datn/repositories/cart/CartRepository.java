package com.example.datn.repositories.cart;

import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart,Integer> {

    @Query("select c from Cart c where c.id = :id")
    Cart findByIdCart(Integer id);

    @Query("select cd from CartDetail cd where cd.cart.id = :id")
    List<CartDetail> findAllCartDetailByCartId(Integer id);

    @Query("select cd.cart from CartDetail cd where cd.id =:id")
    Cart findCartByCartDetailId(Integer id);

    Cart findByAccountId(Integer accountId);

    @Query("SELECT c FROM Cart c WHERE c.account.id = :accountId ORDER BY c.updated_at DESC")
    List<Cart> findCartsByAccountIdOrderByUpdatedAtDesc(@Param("accountId") Integer accountId);
}
