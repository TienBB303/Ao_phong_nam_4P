package com.example.datn.controllers.selling;

import com.example.datn.dto.selling_inline.BillDetailDto;
import com.example.datn.dto.selling_inline.BillSessionDto;
import com.example.datn.dto.selling_inline.ProductDetailDto;
import com.example.datn.entities.Discount;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import com.example.datn.services.BillService;
import com.example.datn.services.CartService;
import com.example.datn.services.DiscountService;
import com.example.datn.services.product_and_other.ProductService;
import com.google.zxing.qrcode.decoder.Mode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        List<Cart> listAllCart = cartService.getAllCarts();
        List<Cart> activeCarts = listAllCart.stream()
                .filter(Cart::getStatus) // true -> chỉ hiện các cart có trạng thái là true lên
                .collect(Collectors.toList());
        model.addAttribute("listCart", activeCarts);
//        model.addAttribute("listCart",listAllCart);
        return "admin/selling/inline";
    }

    @PostMapping("/create-cart")
    public ResponseEntity<?> createCart(){
        try {
            Cart cart = new Cart();
            cart.setTotal_price_cart(BigDecimal.valueOf(0));
            cart.setTotal_quantity(0);
            cart.setCreated_at(new Date());
            cart.setStatus(true);
//            cart.setAccount(); tam thoi null cho den khi lam xong tai khoan
            cartService.SaveCart(cart);
            return ResponseEntity.ok().body("Thêm giỏ hàng thành công");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Thêm giỏ hàng thất bại");
        }
    }

    @GetMapping("cart-detail")
    public String detailCart(@RequestParam("idCart") Integer idCart,
                             Model model,
                             HttpSession session){
        Cart cart = cartService.findCartById(idCart);
        if(cart == null){
            model.addAttribute("alert", "Không có giỏ hàng!");
            model.addAttribute("type", "error");
        }
        List<CartDetail> listCartDetails = cartService.findAllCartDetailsByCartId(idCart);


        List<Cart> listAllCart = cartService.getAllCarts();
        List<Cart> activeCarts = listAllCart.stream()
                .filter(Cart::getStatus)
                .collect(Collectors.toList());
        Integer itemInCart = countItemInCart(idCart);
        Integer allItemInCart = countAllItemInCart(idCart);
        BigDecimal totalPriceInCart = totalPriceInCart(idCart);
        List<Discount> discountCanApply = listDiscountCanApply(totalPriceInCart);
        BigDecimal totalPriceDiscount = cart.getTotal_discount();
        BigDecimal totalPriceCheckOut = cart.getTotal_price_checkout();

        model.addAttribute("listCart",activeCarts);
        model.addAttribute("itemInCart",itemInCart);
        model.addAttribute("allItemInCart",allItemInCart);
        model.addAttribute("totalPriceInCart",totalPriceInCart);
        model.addAttribute("discountCanApply",discountCanApply);
        model.addAttribute("totalPriceDiscount",totalPriceDiscount);
        model.addAttribute("totalPriceCheckOut",totalPriceCheckOut);

        model.addAttribute("idCart", idCart);
        model.addAttribute("cart",cart);
        model.addAttribute("listCartDetail",listCartDetails);
        return "admin/selling/inline";
    }

    @GetMapping("/search-product-detail")
    @ResponseBody
    public List<ProductDetailDto> searchProductDetail(@RequestParam("keyword") String keyword){
        List<ProductDetail> result = productService.searchProductDetail(keyword);
        for (ProductDetail productDetail : result) {
            System.out.println("list product detail result:" + productDetail.getProduct().getName() + productDetail.getColor().getName() + productDetail.getSize().getName());
        }

        return result.stream().map(ProductDetailDto::new).collect(Collectors.toList());
    }

    @PostMapping("/add-to-cart")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestParam("idCart") Integer idCart,
                                       @RequestParam("productDetailId") Integer productDetailId, Model model){
        try {
//            Cart cart = cartService.findCartById(idCart);
            cartService.addProductToCart(idCart,productDetailId);
//            Integer itemInCart = countItemInCart(idCart);
//            Integer allItemInCart = countAllItemInCart(idCart);
//            BigDecimal totalPriceCheckOut = cart.getTotal_price_checkout();
//
//            model.addAttribute("itemInCart",itemInCart);
//            model.addAttribute("allItemInCart",allItemInCart);
//            model.addAttribute("totalPriceCheckOut",totalPriceCheckOut);

            return ResponseEntity.ok().body("Thêm sản phẩm vào giỏ thành công");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage()); // message throw từ service nếu hết hàng
        }
    }

    public Integer countItemInCart(@RequestParam("idCart") Integer idCart){
        if(cartService.findCartById(idCart) == null){
            return 0;
        }else{
            return cartService.countItemInCartByCartId(idCart);
        }
    }

    public Integer countAllItemInCart(@RequestParam("idCart") Integer idCart){
        if(cartService.findCartById(idCart) == null){
            return 0;
        }else{
            return cartService.countAllItemInCartByCartId(idCart);
        }
    }

    public BigDecimal totalPriceInCart(@RequestParam("idCart") Integer idCart){
        if(cartService.findCartById(idCart) == null){
            return new BigDecimal(0);
        }else{
            return cartService.plusAllItemInCartByCartId(idCart);
        }
    }

    @PostMapping("/update-quantity")
    @ResponseBody
    public ResponseEntity<?> upQuantity(@RequestParam("cartDetailId") Integer cartDetailId,@RequestParam("quantity") Integer quantity){
        try {
            cartService.updateQuantityInCart(cartDetailId,quantity);
            return ResponseEntity.ok("thay đổi số lượng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-item")
    @ResponseBody
    public ResponseEntity<?> deleteItemInCart(@RequestParam("cartDetailId") Integer cartDetailId){
        try {
            CartDetail cartDetail = cartService.findCartDetailById(cartDetailId);
            ProductDetail productDetail = cartDetail.getProductDetail();

            cartService.deleteItemFromCart(cartDetailId);
            return ResponseEntity.ok("Xóa sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-cart")
    @ResponseBody
    public ResponseEntity<?> deleteCart(@RequestParam("idCart") Integer idCart){
        try {
            cartService.deleteCart(idCart);
            return ResponseEntity.ok("Xóa giỏ hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/thanh-toan")
    @ResponseBody
    public ResponseEntity<?> checkOut(@RequestParam("idCart") Integer idCart) {
        try {
            billService.checkOut(idCart);
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
            cartService.applyDiscountToCart(idCart, discountId);
            return ResponseEntity.ok("Thêm mã thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
