package com.example.datn.controllers.user;

import com.example.datn.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class shoppingCartController {
    @Autowired
     DiscountService discountService;

    @GetMapping("/shopping-cart")
    public String viewShoppingCart(Model model) {
        return "user/shoping-cart";
    }

}
