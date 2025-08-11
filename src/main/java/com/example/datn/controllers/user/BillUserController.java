package com.example.datn.controllers.user;

import com.example.datn.dto.bill.BillInsert;
import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Discount;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.BillDetailRepository;
import com.example.datn.repositories.BillRepository;
import com.example.datn.repositories.DiscountRepository;
import com.example.datn.repositories.PaymentMethodRepository;
import com.example.datn.repositories.cart.CartDetailRepositoty;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import com.example.datn.services.CartService;
import com.example.datn.services.MailServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

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

    @GetMapping("/thank-you")
    public String viewSuccess() {
        return "user/thankyou";
    }

    @PostMapping("/bill/createNotLogin")
    public String createNotLogin(HttpSession session, BillInsert billInsert, RedirectAttributes redirectAttributes) throws Exception {
        Integer cartId = (Integer) session.getAttribute("cartId");

        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            int totalQuantity = 0;

            for (CartDetail cd : cart.getCartDetails()) {
                if (cd.getQuantity() > cd.getProductDetail().getQuantity()) {
                    redirectAttributes.addFlashAttribute("error",
                            "Số lượng sản phẩm " + cd.getProductDetail().getProduct().getName() + " chỉ còn " + cd.getProductDetail().getQuantity());
                    return "redirect:/cart";
                }
                totalQuantity += cd.getQuantity();
            }

            BigDecimal total = cartService.calTotalCart(cart);
            BigDecimal discountAmount = billInsert.getDiscountValue() != null ? billInsert.getDiscountValue() : BigDecimal.ZERO;

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
            bill.setCustomer(null); // null cho guest shopping
            bill.setTotalAmount(total);
            bill.setDiscountAmount(discountAmount);
            bill.setTotal_checkout(totalCheckout);
            bill.setShippingFee(shippingFee);
            bill.setName(billInsert.getFullName());
            bill.setPhoneNumber(billInsert.getPhone());
            bill.setEmail(billInsert.getEmail());
            bill.setCreatedAt(LocalDateTime.now());
            bill.setUpdatedAt(null);
            bill.setAddress_shipping(
                    billInsert.getStreet() + ", " +
                            billInsert.getWard() + ", " +
                            billInsert.getDistrict() + ", " +
                            billInsert.getProvince());
            bill.setNote(billInsert.getNote());
            bill.setTypeBill(true);
            bill.setStatus(1);
            bill.setPaymentMethod(paymentMethodRepository.findById(1).orElse(null));

            // ✅ Gán discount nếu có discountId
            if (billInsert.getDiscountId() != null) {
                Discount discount = discountRepository.findById(billInsert.getDiscountId()).orElse(null);
                bill.setDiscount(discount);
            } else {
                bill.setDiscount(null);
            }
            billRepository.save(bill);

            for (CartDetail cd : cart.getCartDetails()) {
                BillDetails billDetails = new BillDetails();
                billDetails.setBill(bill);
                billDetails.setProductDetail(cd.getProductDetail());
                billDetails.setPrice(cd.getProductDetail().getPrice());
                billDetails.setTotal_price(cd.getProductDetail().getPrice().multiply(new BigDecimal(cd.getQuantity())));
                billDetails.setQuantity(cd.getQuantity());
                billDetailRepository.save(billDetails);

                ProductDetail productDetail = cd.getProductDetail();
                productDetail.setQuantity(productDetail.getQuantity() - cd.getQuantity());
                productDetailRepository.save(productDetail);
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
        }
        return "redirect:/thank-you";
    }
}
