package com.example.datn.dto.selling_inline;

import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartSession {
    private Cart cart;
    private List<CartDetail>  cartDetails = new ArrayList<>();
}
