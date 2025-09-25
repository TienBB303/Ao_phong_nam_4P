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
import java.util.ArrayList;
import java.util.List;
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
/// ======================== CREATE BILL ==========================
    @PostMapping("/bill/create")
    public String createBill(HttpSession session,
                             @ModelAttribute BillInsert billInsert,
                             RedirectAttributes redirectAttributes) throws Exception {

        Integer cartId = (Integer) session.getAttribute("cartId");
        if (cartId == null) {
            return "redirect:/cart";
        }

        Cart cart = cartService.findCartById(cartId);

        // kiểm tra tồn kho
        for (CartDetail cd : cart.getCartDetails()) {
            if (cd.getQuantity() > cd.getProductDetail().getQuantity()) {
                redirectAttributes.addFlashAttribute("error",
                        "Sản phẩm " + cd.getProductDetail().getProduct().getName() + " Size "+ cd.getProductDetail().getSize().getCode() + "Màu sắc" +cd.getProductDetail().getColor().getName()
                                + " chỉ còn " + cd.getProductDetail().getQuantity() + " sản phẩm.");
                return "redirect:/cart";
            }
        }

        BigDecimal total = cartService.calTotalCart(cart);
        BigDecimal discountAmount = billInsert.getDiscountValue() != null
                ? billInsert.getDiscountValue() : BigDecimal.ZERO;
        if (discountAmount.compareTo(total) > 0) discountAmount = total;

        BigDecimal totalCheckout = total.subtract(discountAmount);
        BigDecimal shippingFee = "Hà Nội".equalsIgnoreCase(billInsert.getProvince())
                ? new BigDecimal(30000) : new BigDecimal(40000);
        totalCheckout = totalCheckout.add(shippingFee);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(billInsert.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Phương thức thanh toán không hợp lệ"));

        // ================== Tiền mặt ==================
        if ("Tiền mặt".equalsIgnoreCase(paymentMethod.getName())) {
            Bill bill = buildBillFromCart(billInsert, cart, paymentMethod, total, discountAmount, totalCheckout, shippingFee);
            bill.setStatus(1);              // chờ xác nhận
            bill.setPaymentStatus(false);   // chưa thanh toán online
            billRepository.save(bill);

            if (billInsert.getDiscountId() != null) {
                discountRepository.findById(billInsert.getDiscountId()).ifPresent(discount -> {
                    if (discount.getUsageLimit() > 0) {
                        discount.setUsageLimit(discount.getUsageLimit() - 1);
                        discountRepository.save(discount);
                    }
                });
            }
            saveBillDetails(bill, cart);


            sendMail(bill);
            clearCart(session, cart);

            return "redirect:/thank-you";
        }

        // ================== Chuyển khoản (MoMo) ==================
        if ("Chuyển khoản".equalsIgnoreCase(paymentMethod.getName())) {
            session.setAttribute("pendingBill", billInsert);
            return "redirect:" + momoOnlineService.momoCreateOdr(cartId, totalCheckout);
        }

        return "redirect:/cart";
    }

    // ======================== RETURN URL (MoMo) ==========================
    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam("resultCode") String resultCode,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Integer cartId = (Integer) session.getAttribute("cartId");
        Cart cart = cartId != null ? cartService.findCartById(cartId) : null;

        System.out.println("MoMo return resultCode=" + resultCode);

        if ("0".equals(resultCode) && cart != null) {
            // ✅ Check lại tồn kho trước khi tạo bill
            for (CartDetail cd : cart.getCartDetails()) {
                if (cd.getQuantity() > cd.getProductDetail().getQuantity()) {
                    redirectAttributes.addFlashAttribute("warning",
                            "Sản phẩm " + cd.getProductDetail().getProduct().getName()
                                    + " không còn đủ số lượng. Liên hệ với cửa hàng để được hoàn tiền.");
                    return "redirect:/cart";
                }
            }
            // ✅ Thanh toán thành công -> tạo bill
            BillInsert billInsert = (BillInsert) session.getAttribute("pendingBill");
            PaymentMethod momoMethod = paymentMethodRepository.findPaymentMethodByName("Chuyển khoản");

            BigDecimal total = cartService.calTotalCart(cart);
            BigDecimal discountAmount = billInsert.getDiscountValue() != null
                    ? billInsert.getDiscountValue() : BigDecimal.ZERO;
            if (discountAmount.compareTo(total) > 0) discountAmount = total;

            BigDecimal totalCheckout = total.subtract(discountAmount);
            BigDecimal shippingFee = "Hà Nội".equalsIgnoreCase(billInsert.getProvince())
                    ? new BigDecimal(30000) : new BigDecimal(40000);
            totalCheckout = totalCheckout.add(shippingFee);

            Bill bill = buildBillFromCart(billInsert, cart, momoMethod, total, discountAmount, totalCheckout, shippingFee);
            bill.setStatus(2);            // đã xác nhận
            bill.setPaymentStatus(true);  // đã thanh toán online
            billRepository.save(bill);

            saveBillDetails(bill, cart);

            // ✅ Trừ tồn kho
            for (BillDetails bd : bill.getBillDetails()) {
                ProductDetail productDetail = bd.getProductDetail();
                productDetail.setQuantity(productDetail.getQuantity() - bd.getQuantity());
                productDetailRepository.save(productDetail);
            }

            billRepository.save(bill);

            if (billInsert.getDiscountId() != null) {
                discountRepository.findById(billInsert.getDiscountId()).ifPresent(discount -> {
                    if (discount.getUsageLimit() > 0) {
                        discount.setUsageLimit(discount.getUsageLimit() - 1);
                        discountRepository.save(discount);
                    }
                });
            }

            sendMail(bill);
            clearCart(session, cart);

            redirectAttributes.addFlashAttribute("success", "Thanh toán thành công!");
            return "redirect:/thank-you";
        }

        // ❌ Thất bại -> quay lại giỏ hàng
        redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại hoặc đã hủy!");
        return "redirect:/cart";
    }


    // ======================== SUPPORT METHODS ==========================
    private Bill buildBillFromCart(BillInsert billInsert,
                                   Cart cart,
                                   PaymentMethod paymentMethod,
                                   BigDecimal total,
                                   BigDecimal discountAmount,
                                   BigDecimal totalCheckout,
                                   BigDecimal shippingFee) {

        Bill bill = new Bill();
        bill.setCode("HD" + System.currentTimeMillis());
        bill.setTotalAmount(total);
        bill.setDiscountAmount(discountAmount);
        bill.setTotal_checkout(totalCheckout);
        bill.setShippingFee(shippingFee);
        bill.setName(billInsert.getFullName());
        bill.setPhoneNumber(billInsert.getPhone());
        bill.setEmail(billInsert.getEmail());
        bill.setDelivery_type(true);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setAddress_shipping(
                billInsert.getStreet() + ", " + billInsert.getWard() + ", " +
                        billInsert.getDistrict() + ", " + billInsert.getProvince());
        bill.setNote(billInsert.getNote());
        bill.setTypeBill(true);
        bill.setStatus(1);
        bill.setPaymentStatus(false);

        if (billInsert.getDiscountId() != null) {
            Discount dc = discountRepository.findById(billInsert.getDiscountId()).orElse(null);
            bill.setDiscount(dc);
        }


        // gán customer nếu login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            Account account = accountRepository.findByEmail(email).orElse(null);
            if (account != null && account.getCustomer() != null) {
                bill.setCustomer(account.getCustomer());
            }
        }

        bill.setPaymentMethod(paymentMethod);
        return bill;
    }

    private void saveBillDetails(Bill bill, Cart cart) {
        List<BillDetails> detailsList = new ArrayList<>();
        for (CartDetail cd : cart.getCartDetails()) {
            BillDetails billDetails = new BillDetails();
            billDetails.setBill(bill);
            billDetails.setProductDetail(cd.getProductDetail());
            billDetails.setPrice(cd.getProductDetail().getPrice());
            billDetails.setTotal_price(cd.getProductDetail().getPrice()
                    .multiply(new BigDecimal(cd.getQuantity())));
            billDetails.setQuantity(cd.getQuantity());
            billDetailRepository.save(billDetails);

            detailsList.add(billDetails);
        }
        bill.setBillDetails(detailsList); // ✅ gắn list vào bill
    }

    private void sendMail(Bill bill) {
        String content = mailServices.buildOrderConfirmationEmailTemplate(
                bill.getCode(),
                bill.getCreatedAt().toString(),
                bill.getTotal_checkout().doubleValue(),
                bill.getAddress_shipping(),
                bill.getNote(),
                bill.getName(),
                "linhtnph31789@fpt.edu.vn"
        );
        mailServices.sendEmail(bill.getEmail(), "Đặt hàng thành công", content, false, true);
    }

    private void clearCart(HttpSession session, Cart cart) {
        cartDetailRepositoty.deleteAll(cart.getCartDetails());
        session.removeAttribute("cartId");
        session.removeAttribute("pendingBill");
    }
}