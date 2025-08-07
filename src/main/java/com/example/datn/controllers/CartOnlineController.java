package com.example.datn.controllers;

import com.example.datn.dto.bill.BillInsert;
import com.example.datn.entities.Account;
import com.example.datn.entities.Discount;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.ShippingAddress;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.DiscountRepository;
import com.example.datn.repositories.cart.CartDetailRepositoty;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartOnlineController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepositoty cartDetailRepositoty;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @GetMapping
    public String showCart(Model model, HttpSession session) {
        Integer cartId = (Integer) session.getAttribute("cartId");
        BillInsert billInsert = new BillInsert();
        boolean isGuest = true;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String email = authentication.getName();
            account = accountRepository.findByEmail(email).orElse(null);

            if (account != null && account.getCustomer() != null) {
                isGuest = false;

                if (cartId == null) {
                    List<Cart> carts = cartRepository.findCartsByAccountIdOrderByUpdatedAtDesc(account.getId());
                    if (!carts.isEmpty()) {
                        Cart latestCart = carts.get(0);
                        cartId = latestCart.getId();
                        session.setAttribute("cartId", cartId);
                    }
                }

                billInsert.setFullName(account.getCustomer().getName());
                billInsert.setPhone(account.getCustomer().getPhoneNumber());
                billInsert.setEmail(account.getEmail());

                if (account.getCustomer().getAddresses() != null && !account.getCustomer().getAddresses().isEmpty()) {
                    ShippingAddress address = account.getCustomer().getAddresses().get(0);
                    billInsert.setProvince(address.getProvinceName());
                    billInsert.setDistrict(address.getDistrictName());
                    billInsert.setWard(address.getWardName());
                    billInsert.setStreet(address.getAddressDetail());
                }
            }
        }

        model.addAttribute("isGuest", isGuest);
        model.addAttribute("billInsert", billInsert);

        if (!isGuest) {
            List<Discount> discounts = discountRepository.findValidDiscounts();
            model.addAttribute("availableDiscounts", discounts);
        }


        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);

            if (cart == null || cart.getCartDetails() == null || cart.getCartDetails().isEmpty()) {
                model.addAttribute("message", "Giỏ hàng của bạn đang trống.");
                return "user/cart-empty";
            }

            model.addAttribute("cart", cart);
            model.addAttribute("totalCart", cartService.calTotalCart(cart));
        } else {
            model.addAttribute("message", "Giỏ hàng của bạn đang trống.");
            return "user/cart-empty";
        }

        model.addAttribute("isGuest", isGuest);

        return "user/cart";
    }

    @GetMapping("/delete-cart")
    public String deleteCart(@RequestParam Integer productDetailId, HttpSession session) {
        Integer cartId = (Integer) session.getAttribute("cartId");
        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            if (cart != null && cart.getCartDetails() != null) {
                for (CartDetail cd : cart.getCartDetails()) {
                    if (cd.getProductDetail().getId().equals(productDetailId)) {
                        cartDetailRepositoty.deleteById(cd.getId());
                        break;
                    }
                }
            }
        }
        return "redirect:/cart";
    }

    @GetMapping("/up-down")
    public String upAndDownQuantity(@RequestParam Integer productDetailId, @RequestParam Integer quantity, HttpSession session) {
        Integer cartId = (Integer) session.getAttribute("cartId");
        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            if (cart != null && cart.getCartDetails() != null) {
                for (CartDetail cd : cart.getCartDetails()) {
                    if (cd.getProductDetail().getId().equals(productDetailId)) {
                        int newQuantity = cd.getQuantity() + quantity;
                        if (newQuantity > 0 && newQuantity <= cd.getProductDetail().getQuantity()) {
                            cd.setQuantity(newQuantity);
                            cartDetailRepositoty.save(cd);
                        }
                    }
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

        Account account = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName(); // hoặc (String) authentication.getPrincipal();
            account = accountRepository.findByEmail(email).orElse(null);
        }

        if (cartId == null) {
            Cart newCart = new Cart();
            newCart.setCreated_at(new Date());
            newCart.setTotal_quantity(0);
            newCart.setTotal_price_cart(BigDecimal.ZERO);
            newCart.setTotal_price_checkout(BigDecimal.ZERO);
            newCart.setTotal_discount(BigDecimal.ZERO);

            // 👉 Gán Account nếu có
            if (account != null) {
                newCart.setAccount(account);
            }

            cartRepository.save(newCart);
            cartId = newCart.getId();
            session.setAttribute("cartId", cartId);
        }

        try {
            // 👉 Gửi account vào service
            cartService.addProductOnlineToCart(cartId, productDetailId, quantity, account);

            if ("buyNow".equals(action)) {
                return "redirect:/cart";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
            return "redirect:/product-detail/" + productDetailId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/product-detail/" + productDetailId;
        }
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {
        Integer cartId = (Integer) session.getAttribute("cartId");
        BillInsert billInsert = new BillInsert();
        boolean isGuest = true;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            account = accountRepository.findByEmail(email).orElse(null);

            if (account != null && account.getCustomer() != null) {
                isGuest = false;

                billInsert.setFullName(account.getCustomer().getName());
                billInsert.setPhone(account.getCustomer().getPhoneNumber());
                billInsert.setEmail(account.getEmail());

                if (account.getCustomer().getAddresses() != null && !account.getCustomer().getAddresses().isEmpty()) {
                    ShippingAddress address = account.getCustomer().getAddresses().get(0);
                    billInsert.setProvince(address.getProvinceName());
                    billInsert.setDistrict(address.getDistrictName());
                    billInsert.setWard(address.getWardName());
                    billInsert.setStreet(address.getAddressDetail());
                }

                // ✅ Lấy danh sách mã giảm giá còn hiệu lực
                List<Discount> discounts = discountRepository.findValidDiscounts();
                model.addAttribute("availableDiscounts", discounts);
            }
        }

        model.addAttribute("isGuest", isGuest);
        model.addAttribute("billInsert", billInsert);

        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            if (cart == null || cart.getCartDetails().isEmpty()) {
                model.addAttribute("message", "Giỏ hàng của bạn đang trống.");
                return "user/cart-empty";
            }

            model.addAttribute("cart", cart);
            model.addAttribute("totalCart", cartService.calTotalCart(cart));
        } else {
            model.addAttribute("message", "Giỏ hàng của bạn đang trống.");
            return "user/cart-empty";
        }

        return "user/checkout";
    }

}

