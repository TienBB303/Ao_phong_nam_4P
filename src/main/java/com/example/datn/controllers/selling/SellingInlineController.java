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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private MomoService momoService;

    @Autowired
    private AccountService accountService;

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

        Integer numberItemInCart = countItemInCart(idCart);                                     // số lượng các sản phẩm trong hóa đơn hiện tại
        Integer allItemInCart = countAllItemInCart(idCart);                                     // tổng tất cả sản phẩm chi tiết trong hóa đơn
        BigDecimal totalPriceInCart = totalPriceInCart(idCart);                                 // tổng tiền của tất cả sp trong hóa đơn
        List<Discount> discountCanApply = listDiscountCanApply(totalPriceInCart);               // hiện các mã được áp dụng
        BigDecimal totalPriceDiscount = cart.getDiscountAmount();                               // tổng tiền được giảm giá
        BigDecimal totalPriceCheckOut = cart.getTotal_checkout();                               // tổng tiền của hóa đơn
        // ship
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
        // ship
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

    @GetMapping("/list-product-search")
    @ResponseBody
    public List<ProductDetailDto> listProductSearch() {
        return productService.getAllProductDetailsInStock()
                .stream()
                .map(ProductDetailDto::new) // constructor map từ entity -> DTO
                .collect(Collectors.toList());
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
            billService.checkOut(idCart, typePayment); // tiền mặt hoặc chuyển khoản
            Bill bill = billService.findById(idCart);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Thanh toán thành công!");
            body.put("billId", bill.getId());
            body.put("billCode", bill.getCode());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-momo-order")
    @ResponseBody
    public ResponseEntity<?> createMomoOrder(@RequestParam Integer idCart) {
        try {
            Bill cart = billService.findCartById(idCart);
            if (cart == null) return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng");

            BigDecimal amount = cart.getTotal_checkout();

            // Service trả về payUrl
            String payUrl = momoService.createQrOrder(idCart, amount);

            Map<String, Object> res = new HashMap<>();
            res.put("payUrl", payUrl);
            res.put("billId", idCart);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @RequestMapping(value = "/momo-ipn", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> momoIpn(@RequestBody(required = false) Map<String, Object> payload,
                                          @RequestParam Map<String, String> params) {
        if ((payload == null || payload.isEmpty()) && (params == null || params.isEmpty())) {
            return ResponseEntity.ok("IPN received without body");
        }

        System.out.println("MOMO IPN payload=" + payload + ", params=" + params);

        String orderId = payload != null ? (String) payload.get("orderId") : params.get("orderId");
        String resultCode = payload != null ? String.valueOf(payload.get("resultCode")) : params.get("resultCode");

        if ("0".equals(resultCode)) {
            Integer cartId = extractCartId(orderId);
            try {
                billService.checkOut(cartId, "Chuyển khoản");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Checkout fail");
            }
        }
        return ResponseEntity.ok("IPN received");
    }

    private Integer extractCartId(String orderId) {
        String idStr = orderId.replace("CART", "").split("_")[0];
        return Integer.parseInt(idStr);
    }

    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam Map<String, String> params, Model model) {
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");

        if ("0".equals(resultCode)) {
            Integer cartId = extractCartId(orderId);
            try {
                billService.checkOut(cartId, "Chuyển khoản");
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("error", "Checkout fail: " + e.getMessage());
                return "admin/selling/momo-result";
            }
        }

        model.addAttribute("success", "0".equals(resultCode));
        model.addAttribute("orderId", orderId);
        return "admin/selling/momo-result"; // view thymeleaf
    }

//    @GetMapping("/thanh-toan")
//    public String redirectThanhToan() {
//        return "redirect:/admin/sell-inline/hien-thi";
//    }

    //Hiện list discount
//    public List<Discount> listDiscountCanApply(BigDecimal minPrice) {
//        return discountService.getAllDiscountByMinPurchase(minPrice);
//    }

    public List<Discount> listDiscountCanApply(BigDecimal totalPrice){
        List<Discount> list = discountService.getAllDiscountByMinPurchase(totalPrice);

        list.sort((d1, d2) -> {
            BigDecimal val1 = calDiscountValue(d1, totalPrice);
            BigDecimal val2 = calDiscountValue(d2, totalPrice);
            return val2.compareTo(val1);
        });
        return list;
    }

    private BigDecimal calDiscountValue(Discount d, BigDecimal totalPrice) {
        BigDecimal discountAmount = BigDecimal.ZERO;

        if(d.getDiscountType().equals("percent")){
            if (d.getDiscountValue() != null && d.getDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = totalPrice.multiply(d.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
            }
        }else if (d.getDiscountType().equals("amount")){
            if (d.getDiscountValue() != null && d.getDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.max(d.getDiscountValue());
            }
        }

        return discountAmount;
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
            Bill bill = billService.addCustomerToCart(idCart, customerId);

            Map<String, Object> response = new HashMap<>();
            response.put("name", bill.getName());
            response.put("phone", bill.getPhoneNumber());
            response.put("address", bill.getAddress_shipping());

            return ResponseEntity.ok(response);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
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
                                      @RequestParam("phoneD") String phoneD,
                                      @RequestParam("addressD") String addressD,
                                      @RequestParam(value = "feeD", required = false) BigDecimal feeD) {
        try {
            billService.delivery(cartId, isDelivery, nameD, phoneD, addressD, feeD);

            Bill bill = billService.findCartById(cartId);

            BigDecimal totalCheckout = bill.getTotal_checkout();

            return ResponseEntity.ok(Map.of(
                    "totalCheckout", totalCheckout,
                    "name", bill.getName(),
                    "phone", bill.getPhoneNumber(),
                    "address", bill.getAddress_shipping(),
                    "fee", bill.getShippingFee()
            ));
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

    @PostMapping("/add-by-barcode")
    public ResponseEntity<?> addByBarcode(@RequestParam("idCart") Integer idCart, @RequestParam("barcode") String barcode) {
        try {
            billService.addProductDetailToCartByBarcode(idCart, barcode);
            return ResponseEntity.ok("thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/print/{id}")
    public String printInvoice(@PathVariable("id") Integer billId, Model model) {
        Account account = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            account = accountService.findByEmail(email);
        }

        String name_employee = account.getCustomer().getName();

        Bill bill = billService.findById(billId);

        Customer customer = null;
        if (bill.getCustomer() != null) {
            customer = customerService.findById(bill.getCustomer().getId());
        }

        List<BillDetails> list_san_pham = billService.findBillDetailsByBillId(billId);

        int tongSoLuong = (bill.getTotal_quantity() != null)
                ? bill.getTotal_quantity()
                : list_san_pham.stream()
                .map(BillDetails::getQuantity)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        // Đảm bảo shippingFee không null
        BigDecimal shippingFee = bill.getShippingFee() == null
                ? BigDecimal.ZERO
                : bill.getShippingFee();

        model.addAttribute("bill", bill);
        model.addAttribute("customer", customer);
        model.addAttribute("list_san_pham", list_san_pham);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("tongSoLuong", tongSoLuong);
        model.addAttribute("name_employee", name_employee);

        return "admin/selling/print";
    }

}