package com.example.datn.controllers.user;

import com.example.datn.dto.bill.BillInsert;
import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.repositories.BillDetailRepository;
import com.example.datn.repositories.BillRepository;
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
    private CartRepository cartRepository;

    @Autowired
    private MailServices mailServices;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @GetMapping("/thanhcong")
    public String viewSuccess(){
        return "user/thankyou";
    }

    @PostMapping("/bill/createNotLogin")
    public String createNotLogin(HttpSession session, BillInsert billInsert, RedirectAttributes redirectAttributes) {
        Integer cartId = (Integer) session.getAttribute("cartId");

        if (cartId != null) {
            Cart cart = cartService.findCartById(cartId);
            int totalQuantity = 0;

            for (CartDetail cd : cart.getCartDetails()) {
                if (cd.getQuantity() > cd.getProductDetail().getQuantity()) {
                    redirectAttributes.addFlashAttribute("error", "Số lượng sản phẩm " + cd.getProductDetail().getProduct().getName() + " chỉ còn " + cd.getProductDetail().getQuantity());
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
            BigDecimal shippingFee = billInsert.getProvince().equals("Hà Nội") ? new BigDecimal(30000) : new BigDecimal(40000);
            totalCheckout = totalCheckout.add(shippingFee);

            // Tạo hóa đơn
            Bill bill = new Bill();
            bill.setCode("HD" + System.currentTimeMillis());
            bill.setCustomer(null);
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
            bill.setTypeBill(true); // Hóa đơn tại website
            bill.setStatus(1); // Trạng thái đơn hàng mới
            bill.setPaymentMethod(paymentMethodRepository.findById(1).orElse(null));
            bill.setDiscount(null);

            billRepository.save(bill);

            for (CartDetail cd : cart.getCartDetails()) {
                BillDetails billDetails = new BillDetails();
                billDetails.setBill(bill);
                billDetails.setProductDetail(cd.getProductDetail());
                billDetails.setPrice(cd.getProductDetail().getPrice());
                billDetails.setTotal_price(cd.getProductDetail().getPrice().multiply(new BigDecimal(cd.getQuantity())));
                billDetails.setQuantity(cd.getQuantity());
                billDetailRepository.save(billDetails);
                productDetailRepository.save(cd.getProductDetail());
            }

            String content = mailServices.buildOrderConfirmationEmailTemplate(
                    bill.getCode(),
                    bill.getCreatedAt().toString(),
                    bill.getTotal_checkout().doubleValue(),
                    bill.getAddress_shipping(),
                    bill.getNote(),
                    bill.getName(),
                    "thaitvph40872@fpt.edu.vn");
            mailServices.sendEmail(billInsert.getEmail(), "Đặt hàng thành công", content, false, true);
        }

        return "redirect:/thanhcong";
    }
}
