package com.example.datn.controllers.selling;

import com.example.datn.dto.selling_inline.BillDetailDto;
import com.example.datn.dto.selling_inline.BillSessionDto;
import com.example.datn.dto.selling_inline.CustomerDto;
import com.example.datn.dto.selling_inline.ProductDetailDto;
import com.example.datn.entities.*;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import com.example.datn.services.*;
import com.example.datn.services.product_and_other.ProductService;
import com.google.zxing.qrcode.decoder.Mode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/sell-inline")
public class SellingInlineController {
    @Autowired
    ProductService productService;

    @Autowired
    private BillService billService;

    @Autowired
    private CartService cartService;

    @Autowired
    private DiscountService  discountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @ModelAttribute("listProduct")
    public List<Product> listProduct() {
        return productService.getAll();
    }

    @ModelAttribute("listProductDetail")
    public List<ProductDetail> listProductDetail() {
        return productService.getAllProductDetails();
    }

    @GetMapping("/hien-thi")
    public String sellInlineView( Model model, HttpSession session){
        List<Bill> listAllCart = billService.getAllCartInline();

        model.addAttribute("listCart", listAllCart);
        return "admin/selling/inline";
    }

    @PostMapping("/create-cart")
    public ResponseEntity<?> createCart(){
        try {
            Bill bill = new Bill();
            bill.setCode(billService.taoMaTuDongBill());
            bill.setTotalAmount(BigDecimal.ZERO);
            bill.setTotal_quantity(0);
            bill.setCreatedAt(LocalDateTime.now());
            bill.setStatus(9);
            bill.setTypeBill(false);
            bill.setPaymentStatus(false);
            bill.setDelivery_type(false);
            PaymentMethod tienMat = paymentMethodService.getAllPaymentMethods().get(0);
            bill.setPaymentMethod(tienMat);
            billService.save(bill);
            return ResponseEntity.ok().body("Thêm giỏ hàng thành công");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Thêm giỏ hàng thất bại");
        }
    }

    @GetMapping("cart-detail")
    public String detailCart(@RequestParam("idCart") Integer idCart,
                             Model model,
                             HttpSession session){
        Bill cart = billService.findCartById(idCart);

        if(cart == null){
            model.addAttribute("alert", "Không có giỏ hàng!");
            model.addAttribute("type", "error");
        }

        List<BillDetails> listCartDetails = billService.findAllCartDetailsByCartId(idCart);

        List<Bill> listAllCart = billService.getAllCartInline();
        Customer customer = cart.getCustomer();
        if (customer != null){
            String customerName = customer.getName() + " - " + customer.getPhoneNumber();
            model.addAttribute("customerName",customerName);
        }

        Integer numberItemInCart = countItemInCart(idCart);
        Integer allItemInCart = countAllItemInCart(idCart);
        BigDecimal totalPriceInCart = totalPriceInCart(idCart);
        List<Discount> discountCanApply = listDiscountCanApply(totalPriceInCart);
        BigDecimal totalPriceDiscount = cart.getDiscountAmount();
        BigDecimal totalPriceCheckOut = cart.getTotal_checkout();
        String nameShip = cart.getName();
        String phoneShip = cart.getPhoneNumber();
        String addressShip = cart.getAddress_shipping();
        boolean isDelivery = cart.getDelivery_type() != null && cart.getDelivery_type();
        BigDecimal feeShip = cart.getShippingFee();


        model.addAttribute("listCart",listAllCart);
        model.addAttribute("itemInCart",numberItemInCart);
        model.addAttribute("allItemInCart",allItemInCart);
        model.addAttribute("totalPriceInCart",totalPriceInCart);
        model.addAttribute("discountCanApply",discountCanApply);
        model.addAttribute("totalPriceDiscount",totalPriceDiscount);
        model.addAttribute("totalPriceCheckOut",totalPriceCheckOut);
        model.addAttribute("nameShip",nameShip);
        model.addAttribute("phoneShip",phoneShip);
        model.addAttribute("addressShip",addressShip);
        model.addAttribute("isDelivery", isDelivery);
        model.addAttribute("feeShip", feeShip);

        model.addAttribute("idCart", idCart);
        model.addAttribute("cart",cart);
        model.addAttribute("listCartDetail",listCartDetails);

        return "admin/selling/inline";
    }

    @GetMapping("/search-product-detail")
    @ResponseBody
    public List<ProductDetailDto> searchProductDetail(@RequestParam("keyword") String keyword){
        List<ProductDetail> resultSearch = productService.searchProductDetail(keyword);
        for (ProductDetail productDetail : resultSearch) {
            System.out.println("list product detail result:" + productDetail.getProduct().getName() + productDetail.getColor().getName() + productDetail.getSize().getName());
        }

        return resultSearch.stream().map(ProductDetailDto::new).collect(Collectors.toList());
    }

    @GetMapping("/search-customer-inline")
    @ResponseBody
    public List<CustomerDto> searchCustomerInline(@RequestParam("keyword") String keyword){
        List<Customer> resultSearch = customerService.searchCustomerInline(keyword);
        return resultSearch.stream().map(CustomerDto::new).collect(Collectors.toList());
    }


    @PostMapping("/add-to-cart")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestParam("idCart") Integer idCart,
                                       @RequestParam("productDetailId") Integer productDetailId){
        try {
            billService.addProductToCart(idCart,productDetailId);

            return ResponseEntity.ok().body("Thêm sản phẩm vào giỏ thành công");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage()); // message throw từ service nếu hết hàng
        }
    }

    public Integer countItemInCart(@RequestParam("idCart") Integer idCart){
        if(billService.findCartById(idCart) == null){
            return 0;
        }else{
            return billService.countItemInCartByCartId(idCart);
        }
    }

    public Integer countAllItemInCart(@RequestParam("idCart") Integer idCart){
        if(billService.findCartById(idCart) == null){
            return 0;
        }else{
            return billService.countAllItemInCartByCartId(idCart);
        }
    }

    public BigDecimal totalPriceInCart(@RequestParam("idCart") Integer idCart){
        if(billService.findCartById(idCart) == null){
            return new BigDecimal(0);
        }else{
            return billService.plusAllItemInCartByCartId(idCart);
        }
    }

    @PostMapping("/update-quantity")
    @ResponseBody
    public ResponseEntity<?> upQuantity(@RequestParam("cartDetailId") Integer cartDetailId,@RequestParam("quantity") Integer quantity){
        try {
            billService.updateQuantityInCart(cartDetailId,quantity);
            return ResponseEntity.ok("thay đổi số lượng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-item")
    @ResponseBody
    public ResponseEntity<?> deleteItemInCart(@RequestParam("cartDetailId") Integer cartDetailId){
        try {
            BillDetails cartDetail = billService.findCartDetailById(cartDetailId);
            ProductDetail productDetail = cartDetail.getProductDetail();

            billService.deleteItemFromCart(cartDetailId);
            return ResponseEntity.ok("Xóa sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-cart")
    @ResponseBody
    public ResponseEntity<?> deleteCart(@RequestParam("idCart") Integer idCart){
        try {
            billService.deleteCart(idCart);
            return ResponseEntity.ok("Xóa giỏ hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/thanh-toan")
    @ResponseBody
    public ResponseEntity<?> checkOut(@RequestParam("idCart") Integer idCart,
                                      @RequestParam("typePayment") String typePayment) {
        try {
            billService.checkOut(idCart, typePayment);
            return ResponseEntity.ok("Thanh toán thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/thanh-toan")
    public String redirectThanhToan() {
        return "redirect:/admin/sell-inline/hien-thi";
    }

    //Hiện list discount
    public List<Discount> listDiscountCanApply(BigDecimal minPrice) {
        return discountService.getAllDiscountByMinPurchase(minPrice);
    }

    @PostMapping("/apply-discount")
    @ResponseBody
    public ResponseEntity<?> applyDiscountToCart(@RequestParam("idCart") Integer idCart,
                                                 @RequestParam("discountId") Integer discountId) {
        try {
            billService.applyDiscountToCart(idCart, discountId);
            return ResponseEntity.ok("Áp dụng mã giảm giá thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove-discount")
    @ResponseBody
    public ResponseEntity<?> removeDiscount(@RequestParam("idCart") Integer idCart){
        try {
            billService.removeDiscountFromCart(idCart);
            return ResponseEntity.ok("Bỏ mã giảm giá thành công");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/add-customer-to-cart")
    @ResponseBody
    public ResponseEntity<?> addCustomertoCart(@RequestParam("idCart") Integer idCart,
                                               @RequestParam("customerId") Integer customerId) {
        try {
            billService.addCustomerToCart(idCart, customerId);
            return ResponseEntity.ok("Thành công");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/remove-customer-from-cart")
    @ResponseBody
    public ResponseEntity<?> removeCustomerFromCart(@RequestParam("cartId") Integer cartId) {
        try {
            billService.removeCustomerFromCart(cartId);
            return ResponseEntity.ok("Đã xóa khách khỏi cart");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/delivery")
    @ResponseBody
    public ResponseEntity<?> delivery(@RequestParam("cartId") Integer cartId,
                                      @RequestParam("isDelivery") boolean isDelivery,
                                      @RequestParam("nameD") String nameD,
                                      @RequestParam("phoneD")String phoneD,
                                      @RequestParam("addressD")String addressD,
                                      @RequestParam(value = "feeD", required = false)BigDecimal feeD ) {
        try {
            billService.delivery(cartId, isDelivery, nameD, phoneD, addressD, feeD);
            return ResponseEntity.ok("");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add-customer")
    @ResponseBody
    public ResponseEntity<?> addCustomerInline(@RequestBody CustomerDto customerDto) {
        try {
            Customer customer = customerService.createCustomerInline(customerDto);
            Map<String, Object> response = new HashMap<>();
            response.put("id", customer.getId());
            response.put("name", customer.getName() + " - " + customer.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}