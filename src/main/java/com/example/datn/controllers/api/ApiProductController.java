package com.example.datn.controllers.api;

import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.services.AccountService;
import com.example.datn.services.CartService;
import com.example.datn.services.product_and_other.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product-detail")
public class ApiProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private AccountService accountService;
    @Autowired
    private CartService cartService;

    @GetMapping("/price")
    public ResponseEntity<?> getPriceAndQuantity(
            @RequestParam Integer productId,
            @RequestParam Integer colorId,
            @RequestParam Integer sizeId) {

        ProductDetail detail = productService.findProductDetailByColorAndSize(productId, colorId, sizeId);

        if (detail == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("price", detail.getPrice());
        data.put("quantity", detail.getQuantity());
        return ResponseEntity.ok(data);
    }


    @GetMapping("/info-product-detail")
    public ResponseEntity<?>getProductDetailInfo(@RequestParam Integer productID, @RequestParam Integer colorID, @RequestParam Integer sizeID){
        ProductDetail productDetail = productService.findProductDetailByColorAndSize(productID, colorID, sizeID);
        if(productDetail == null){
            return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", productDetail.getId());
        data.put("price", productDetail.getPrice());
        data.put("quantity", productDetail.getQuantity());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<?> addToCart(@RequestParam Integer productDetailId, @RequestParam Integer quantity, HttpSession session, RedirectAttributes redirectAttributes){
        try {
            // 1. Lấy thông tin account từ SecurityContext (nếu có)
            Account account = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String email = authentication.getName();
                account = accountService.findByEmail(email);
            }

            // 2. Lấy cartId từ session
            Integer cartId = (Integer) session.getAttribute("cartId");

            Cart cart;
            if (cartId == null) {
                // Nếu chưa có giỏ → tạo mới
                cart = new Cart();
                cart.setCreated_at(new Date());
                cart.setTotal_quantity(0);
                cart.setTotal_price_cart(BigDecimal.ZERO);
                cart.setTotal_discount(BigDecimal.ZERO);
                cart.setTotal_price_checkout(BigDecimal.ZERO);

                // Nếu đã đăng nhập thì gán account
                if (account != null) {
                    cart.setAccount(account);
                }

                cartService.SaveCart(cart);
                cartId = cart.getId();
                session.setAttribute("cartId", cartId);
            } else {
                cart = cartService.findCartById(cartId);
                if (cart == null) {
                    return ResponseEntity.badRequest().body("Giỏ hàng không tồn tại!");
                }
            }

//
//            Customer userCustomer = (Customer) session.getAttribute("userCustomer");
//            if (userCustomer == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body("Bạn cần đăng nhập để thêm giỏ hàng");
//            }
//
//            Account account = userCustomer.getAccount();
//            Cart cart = accountService.getCartByAccountID(account.getId());
//            if (cart == null) {
//                cart = cartService.createCartForAccount(account); // Tạo mới nếu chưa có
//            }

            ProductDetail productDetail = productService.findProductDetailById(productDetailId);
            if (productDetail == null) {
                return ResponseEntity.badRequest().body("Sản phẩm không tồn tại!");
            }

            // Thêm vào giỏ

            cartService.addProductOnlineToCart(cart.getId(), productDetail.getId(), quantity, account);
            return ResponseEntity.ok("Đã thêm sản phẩm "
                    + productDetail.getProduct().getName()
                    + " (" + productDetail.getColor().getName()
                    + "-" + productDetail.getSize().getCode() + ") vào giỏ hàng");


        }catch (Exception e){
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

}
