package com.example.datn.controllers.user;

import com.example.datn.dto.bill.BillInsert;
import com.example.datn.entities.*;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.*;
import com.example.datn.repositories.cart.CartDetailRepositoty;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import com.example.datn.services.CartService;
import com.example.datn.services.MailServices;
import com.example.datn.services.MomoOnlineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class BillUserController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillDetailRepository billDetailRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartDetailRepositoty cartDetailRepositoty;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private MailServices mailServices;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private MomoOnlineService momoOnlineService;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/thank-you")
    public String viewSuccess() {
        return "user/thankyou";
    }

    @PostMapping("/bill/create")
    public String createBill(HttpSession session,
                             @ModelAttribute BillInsert billInsert,
                             RedirectAttributes redirectAttributes) throws Exception {

        Integer cartId = (Integer) session.getAttribute("cartId");
        if (cartId == null) {
            return "redirect:/cart";
        }

        Cart cart = cartService.findCartById(cartId);
        int totalQuantity = 0;

        for (CartDetail cd : cart.getCartDetails()) {
            if (cd.getQuantity() > cd.getProductDetail().getQuantity()) {
                redirectAttributes.addFlashAttribute("error",
                        "Số lượng sản phẩm " + cd.getProductDetail().getProduct().getName()
                                + " chỉ còn " + cd.getProductDetail().getQuantity());
                return "redirect:/cart";
            }
            totalQuantity += cd.getQuantity();
        }

        BigDecimal total = cartService.calTotalCart(cart);
        BigDecimal discountAmount = billInsert.getDiscountValue() != null
                ? billInsert.getDiscountValue() : BigDecimal.ZERO;
        if (discountAmount.compareTo(total) > 0) {
            discountAmount = total;
        }

        BigDecimal totalCheckout = total.subtract(discountAmount);
        BigDecimal shippingFee = "Hà Nội".equalsIgnoreCase(billInsert.getProvince())
                ? new BigDecimal(30000)
                : new BigDecimal(40000);

        totalCheckout = totalCheckout.add(shippingFee);

        Bill bill = new Bill();
        bill.setCode("HD" + System.currentTimeMillis());
        bill.setTotalAmount(total);
        bill.setDiscountAmount(discountAmount);
        bill.setTotal_checkout(totalCheckout);
        bill.setShippingFee(shippingFee);
        bill.setName(billInsert.getFullName());
        bill.setPhoneNumber(billInsert.getPhone());
        bill.setEmail(billInsert.getEmail());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setDelivery_type(true);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(null);
        bill.setAddress_shipping(
                billInsert.getStreet() + ", " +
                        billInsert.getWard() + ", " +
                        billInsert.getDistrict() + ", " +
                        billInsert.getProvince());
        bill.setNote(billInsert.getNote());
        bill.setTypeBill(true);

        if (billInsert.getPaymentMethodId() == 2) {
            bill.setStatus(2);
            bill.setPaymentStatus(true);
        } else {
            bill.setStatus(1);
            bill.setPaymentStatus(false);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Account account = accountRepository.findByEmail(email).orElse(null);
            if (account != null && account.getCustomer() != null) {
                bill.setCustomer(account.getCustomer());
            }
        } else {
            bill.setCustomer(null);
        }


        PaymentMethod paymentMethod = paymentMethodRepository.findById(billInsert.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ"));
        bill.setPaymentMethod(paymentMethod);

        if (billInsert.getDiscountId() != null) {
            Discount discount = discountRepository.findById(billInsert.getDiscountId()).orElse(null);
            bill.setDiscount(discount);
        }

        billRepository.save(bill);

        for (CartDetail cd : cart.getCartDetails()) {
            BillDetails billDetails = new BillDetails();
            billDetails.setBill(bill);
            billDetails.setProductDetail(cd.getProductDetail());
            billDetails.setPrice(cd.getProductDetail().getPrice());
            billDetails.setTotal_price(cd.getProductDetail().getPrice()
                    .multiply(new BigDecimal(cd.getQuantity())));
            billDetails.setQuantity(cd.getQuantity());
            billDetailRepository.save(billDetails);
            ProductDetail productDetail = cd.getProductDetail();
            if (billInsert.getPaymentMethodId() == 2){
                productDetail.setQuantity(productDetail.getQuantity() - cd.getQuantity());
            }
            productDetailRepository.save(productDetail);
        }

        if ("Chuyển khoản".equalsIgnoreCase(paymentMethod.getName())) {
            String payUrl = momoOnlineService.createQrOrder(bill.getId(), bill.getTotal_checkout());
            return "redirect:" + payUrl;
        }

        String content = mailServices.buildOrderConfirmationEmailTemplate(
                bill.getCode(),
                bill.getCreatedAt().toString(),
                bill.getTotal_checkout().doubleValue(),
                bill.getAddress_shipping(),
                bill.getNote(),
                bill.getName(),
                "thaitvph40872@fpt.edu.vn"
        );
        mailServices.sendEmail(billInsert.getEmail(), "Đặt hàng thành công", content, false, true);

        cartDetailRepositoty.deleteAll(cart.getCartDetails());
        session.removeAttribute("cartId");

        return "redirect:/thank-you";
    }

    //    fill information user login
//    @GetMapping("/checkout")
//    public String checkout(HttpSession session, Model model) {
//        BillInsert billInsert = new BillInsert();
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
//            Account account = (Account) auth.getPrincipal();
//            Customer customer = customerRepository.findByAccount(account);
//
//            if (customer != null) {
//                billInsert.setFullName(customer.getName());
//                billInsert.setPhone(customer.getPhoneNumber());
//                billInsert.setEmail(account.getEmail());
//
//                // Lấy địa chỉ mặc định
//                if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
//                    ShippingAddress defaultAddress = customer.getAddresses()
//                            .stream()
//                            .filter(ShippingAddress::getIsDefault) // nếu có cờ mặc định
//                            .findFirst()
//                            .orElse(customer.getAddresses().get(0));
//
//                    billInsert.setProvince(defaultAddress.getProvinceName());
//                    billInsert.setDistrict(defaultAddress.getDistrictName());
//                    billInsert.setWard(defaultAddress.getWardName());
//                }
//            }
//
//            model.addAttribute("isGuest", false);
//        } else {
//            model.addAttribute("isGuest", true);
//        }
//
//        // giỏ hàng
//        Integer cartId = (Integer) session.getAttribute("cartId");
//        if (cartId != null) {
//            Cart cart = cartService.findCartById(cartId);
//            model.addAttribute("cart", cart);
//            model.addAttribute("totalCart", cartService.calTotalCart(cart));
//        }
//
//        model.addAttribute("billInsert", billInsert);
//
//        return "user/cart";
//    }


    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam Map<String, String> params,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");

        try {
            String[] parts = orderId.split("_");
            String billIdStr = parts[0].replace("CART", "");
            Integer billId = Integer.valueOf(billIdStr);

            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if ("0".equals(resultCode)) {
                bill.setStatus(2);
                bill.setUpdatedAt(LocalDateTime.now());
                billRepository.save(bill);

                session.removeAttribute("cartId");

                redirectAttributes.addFlashAttribute("success", "Thanh toán thành công!");
            } else {
                bill.setStatus(3);
                bill.setUpdatedAt(LocalDateTime.now());
                billRepository.save(bill);

                redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý MoMo: " + e.getMessage());
        }

        return "redirect:/thank-you";
    }

    // ✅ NotifyUrl (server-to-server, MoMo gọi về xác nhận thanh toán)
    @PostMapping("/momo-ipn")
    @ResponseBody
    public ResponseEntity<?> momoIpn(@RequestBody Map<String, Object> payload) {
        try {
            String resultCode = String.valueOf(payload.get("resultCode"));
            String orderId = String.valueOf(payload.get("orderId"));

            String[] parts = orderId.split("_");
            String billIdStr = parts[0].replace("CART", "");
            Integer billId = Integer.valueOf(billIdStr);

            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            if ("0".equals(resultCode)) {
                bill.setStatus(2);
            } else {
                bill.setStatus(3);
            }
            bill.setUpdatedAt(LocalDateTime.now());
            billRepository.save(bill);

            return ResponseEntity.ok(Map.of("message", "IPN received"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
