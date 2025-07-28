package com.example.datn.controllers;

import com.example.datn.dto.bill.BillInsert;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.repositories.cart.CartDetailRepositoty;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Date;

@Controller
@RequestMapping("/cart")
public class CartOnlineController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepositoty cartDetailRepositoty;

    @GetMapping
    public String showCart(Model model, HttpSession session) {
        Integer cartId = (Integer) session.getAttribute("cartId");
        model.addAttribute("billInsert", new BillInsert());
        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            model.addAttribute("cart", cart);
            model.addAttribute("totalCart", cartService.calTotalCart(cart));
        }
        return "user/cart";
    }

    @GetMapping("/delete-cart")
    public String deleteCart(@RequestParam Integer productDetailId, HttpSession session){
        Integer cartId = (Integer) session.getAttribute("cartId");
        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            for(CartDetail cd : cart.getCartDetails()){
                if(cd.getProductDetail().getId() == productDetailId){
                    System.out.println("delete success: "+productDetailId);
                    cartDetailRepositoty.deleteById(cd.getId());
                    break;
                }
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/add")
    public String handleAddToCartOrBuyNow(
            @RequestParam("productDetailId") Integer productDetailId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("action") String action,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Integer cartId = (Integer) session.getAttribute("cartId");

        if (cartId == null) {
            Cart newCart = new Cart();
            newCart.setCreated_at(new Date());
            newCart.setTotal_quantity(0);
            newCart.setTotal_price_cart(BigDecimal.ZERO);
            newCart.setTotal_price_checkout(BigDecimal.ZERO);
            newCart.setTotal_discount(BigDecimal.ZERO);
            cartRepository.save(newCart);

            cartId = newCart.getId();
            session.setAttribute("cartId", cartId);
        }

        try {
            cartService.addProductOnlineToCart(cartId, productDetailId, quantity);

            if ("buyNow".equals(action)) {
                return "redirect:/cart"; // Điều hướng đến trang giỏ nếu là Mua ngay
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
            return "redirect:/product-detail/" + productDetailId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/product-detail/" + productDetailId;
        }
    }

    @GetMapping("/up-down")
    public String upAndDownQuantity(@RequestParam Integer productDetailId, @RequestParam Integer quantity, HttpSession session){
        Integer cartId = (Integer) session.getAttribute("cartId");
        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            for(CartDetail cd : cart.getCartDetails()){
                if(cd.getProductDetail().getId() == productDetailId){
                    cd.setQuantity(cd.getQuantity() + quantity);
                    if(cd.getQuantity() > 0 && cd.getQuantity() <= cd.getProductDetail().getQuantity()){
                        cartDetailRepositoty.save(cd);
                    }
                }
            }
        }
        return "redirect:/cart";
    }
}

