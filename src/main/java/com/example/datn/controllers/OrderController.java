//package com.example.datn.controllers;
//
//import com.example.datn.entities.Bill;
//import com.example.datn.entities.Selling.Cart;
//import com.example.datn.services.BillService;
//import com.example.datn.services.CartService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//
//@Controller
//@RequestMapping("/order")
//public class OrderController {
//
//    @Autowired
//    private CartService cartService;
//
//    @Autowired
//    private BillService billService;
//
//    @PostMapping
//    public String placeOrder(@RequestParam String fullName,
//                             @RequestParam String phone,
//                             @RequestParam String email,
//                             @RequestParam String address,
//                             @RequestParam(required = false) String note,
//                             @RequestParam String paymentMethod,
//                             HttpSession session,
//                             RedirectAttributes redirectAttributes) throws Exception {
//
//        Integer cartId = (Integer) session.getAttribute("cartId");
//        if (cartId == null) {
//            redirectAttributes.addFlashAttribute("error", "Không tìm thấy giỏ hàng");
//            return "redirect:/checkout";
//        }
//
//        Cart cart = cartService.findCartById(cartId);
//        if (cart == null || cart.getCartDetails().isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
//            return "redirect:/checkout";
//        }
//
//        // Tạo hóa đơn (bill)
//        Bill bill = new Bill();
//        bill.setName(fullName);
//        bill.setPhoneNumber(phone);
//        bill.setEmail(email);
//        bill.setAddress_shipping(address);
//        bill.setNote(note);
////        bill.setPaymentMethod(paymentMethod);
//        bill.setTotal_checkout(cart.getTotal_price_checkout());
//        bill.setCreatedAt(LocalDateTime.now());
//        bill.setTypeBill(true);
//        bill.setStatus(1);
//
//        billService.saveBillWithDetails(bill, cart); // Xử lý lưu bill + bill_detail
//
//        // Xóa giỏ hàng sau khi đặt hàng
//        cartService.deleteCart(cartId);
//
//        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
//        return "redirect:/thank-you";
//    }
//}
//
