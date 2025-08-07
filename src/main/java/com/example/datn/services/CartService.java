package com.example.datn.services;

import com.example.datn.entities.Account;
import com.example.datn.entities.Discount;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.DiscountRepository;
import com.example.datn.repositories.cart.CartDetailRepositoty;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartDetailRepositoty cartDetailRepositoty;

    @Autowired
    ProductDetailRepository productDetailRepository;

    @Autowired
    CustomerService customerService;

    @Autowired
    private DiscountService  discountService;

    @Autowired
    private DiscountRepository discountRepository;

    public List<Cart> getAllCarts(){
        return cartRepository.findAll();
    }

    public Cart SaveCart(Cart cart){
        return cartRepository.save(cart);
    }

    public Cart findCartById(Integer idCart){
        return cartRepository.findByIdCart(idCart);
    }

    public BigDecimal calTotalCart(Cart cart){
        BigDecimal total = new BigDecimal(0);
        for(CartDetail cd : cart.getCartDetails()){
            total = total.add( cd.getProductDetail().getPrice().multiply(new BigDecimal(cd.getQuantity())) );
        }
        return total;
    }

    public CartDetail findCartDetailById(Integer idCartDetail){
        return cartDetailRepositoty.findCartDetailById(idCartDetail);
    }

    public Cart findCartByCartDetailId(Integer idCartDetail){
        return cartRepository.findCartByCartDetailId(idCartDetail);
    }

    public List<CartDetail> findAllCartDetailsByCartId(Integer idCart){
        return  cartRepository.findAllCartDetailByCartId(idCart);
    }

    public void addProductToCart(Integer cartId, Integer productDetailId) throws Exception{
        Cart cart = cartRepository.findByIdCart(cartId);
        ProductDetail  productDetail = productDetailRepository.findProductDetailById(productDetailId);
        CartDetail itemExisted = cartDetailRepositoty.findByCartAndProductDetailId(cartId,productDetailId);

        if(productDetail.getQuantity() <= 0){
            throw new Exception("Sản phẩm " +productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" + " đã hết hàng");
        }
        if (itemExisted != null){
            if(productDetail.getQuantity() < 1){
                throw new Exception("Sản phẩm " +productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" + " đã hết hàng");
            }
            itemExisted.setQuantity(itemExisted.getQuantity() + 1);
            itemExisted.setTotal_price(productDetail.getPrice().multiply(BigDecimal.valueOf(itemExisted.getQuantity())));
            productDetail.setQuantity(productDetail.getQuantity() - 1);
            productDetailRepository.save(productDetail);
            cartDetailRepositoty.save(itemExisted);
        }else{
            if(productDetail.getQuantity() < 1){
                throw new Exception("Sản phẩm " +productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" + " đã hết hàng");
            }
            CartDetail cartDetail = new CartDetail();
            cartDetail.setCart(cart);
            cartDetail.setProductDetail(productDetail);
            cartDetail.setQuantity(1);
            cartDetail.setPrice(productDetail.getPrice());
            cartDetail.setTotal_price(productDetail.getPrice());

            productDetail.setQuantity(productDetail.getQuantity() - 1);
            productDetailRepository.save(productDetail);
            cartDetailRepositoty.save(cartDetail);
        }

        List<CartDetail> listCartDetails = cartRepository.findAllCartDetailByCartId(cartId);
        BigDecimal totalPrice = listCartDetails.stream().map(CartDetail::getTotal_price).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalQuantity = listCartDetails.stream().mapToInt(CartDetail::getQuantity).sum();

        cart.setTotal_price_cart(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        recalculateCartTotalWithDiscount(cart);

        cart.setUpdated_at(new Date());
        cartRepository.save(cart);
    }

    public Integer countItemInCartByCartId(Integer idCart){
        return cartDetailRepositoty.countItemInCartByCartId(idCart);
    }

    public Integer countAllItemInCartByCartId(Integer idCart){
        return cartDetailRepositoty.countAllItemInCartByCartId(idCart);
    }

    public BigDecimal plusAllItemInCartByCartId(Integer idCart){
        Cart cart = cartRepository.findByIdCart(idCart);
        List<CartDetail> listCartDetails = cartRepository.findAllCartDetailByCartId(cart.getId());
        BigDecimal totalPriceInCart = BigDecimal.valueOf(0);
        for (CartDetail pd : listCartDetails){
            totalPriceInCart = totalPriceInCart.add(pd.getTotal_price());
        }
        return totalPriceInCart;
    }

    public void updateQuantityInCart(Integer idCartDetail,Integer newQuantity) throws Exception{
        CartDetail cartDetail = cartDetailRepositoty.findById(idCartDetail).orElseThrow(() -> new Exception("Không tìm thấy giỏ hàng"));

        ProductDetail productDetail = cartDetail.getProductDetail();
        if(newQuantity == null){
            throw new Exception("Số lượng không để trống");
        }
        if(newQuantity < 1){
            throw new Exception("Số lượng không được nhỏ hơn 1");
        }
        Integer oldQuantity = cartDetail.getQuantity();
        Integer currentQuantity = productDetail.getQuantity();

        Integer change = newQuantity - oldQuantity; // lay so moi - so luong xu, khong dung tru luon so luong moi, sẽ bị sai
        if(change > 0 && change > currentQuantity){
            throw new Exception("Số lượng tồn kho không đủ cho " + "sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")");
        }
        productDetail.setQuantity(productDetail.getQuantity() - change);
        productDetailRepository.save(productDetail);

        cartDetail.setQuantity(newQuantity);
        cartDetail.setTotal_price(productDetail.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        cartDetailRepositoty.save(cartDetail);

        Cart cart = cartDetail.getCart();
        List<CartDetail> listCartDetails = cartRepository.findAllCartDetailByCartId(cart.getId());
        BigDecimal totalPrice = listCartDetails.stream().map(CartDetail::getTotal_price).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalQuantity = listCartDetails.stream().mapToInt(CartDetail::getQuantity).sum();

        cart.setTotal_price_cart(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        recalculateCartTotalWithDiscount(cart);

        cart.setUpdated_at(new Date());
        cartRepository.save(cart);
    }

    public void deleteItemFromCart(Integer idCartDetail)  throws Exception{
        CartDetail cartDetail = cartDetailRepositoty.findById(idCartDetail).orElseThrow(() -> new Exception("Không tìm thấy sản phẩm trong giỏ"));
        if(cartDetail == null){
            throw new Exception("Không tìm thấy sản phẩm trong giỏ");
        }

        ProductDetail productDetail = cartDetail.getProductDetail();
        productDetail.setQuantity(productDetail.getQuantity() + cartDetail.getQuantity()); // cập nhaarth lại sl
        productDetailRepository.save(productDetail);

        Cart cart = cartDetail.getCart();
        cartDetailRepositoty.delete(cartDetail);

        List<CartDetail> listCartDetails = cartRepository.findAllCartDetailByCartId(cart.getId());
        BigDecimal totalPrice = listCartDetails.stream().map(CartDetail::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalQuantity = listCartDetails.stream().mapToInt(CartDetail::getQuantity).sum();

        cart.setTotal_price_cart(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        recalculateCartTotalWithDiscount(cart);

        cart.setUpdated_at(new Date());
        cartRepository.save(cart);
    }

    public void deleteCart(Integer cartId) throws Exception{
        Cart cart =  cartRepository.findByIdCart(cartId);
        if(cart == null){
            throw new Exception("Không tồn tại giỏ hàng");
        }

        List<CartDetail> listCartDetails = cartRepository.findAllCartDetailByCartId(cartId);
        for(CartDetail cartDetail : listCartDetails){
            ProductDetail productDetail = cartDetail.getProductDetail();
            productDetail.setQuantity(productDetail.getQuantity() + cartDetail.getQuantity()); //hoàn lại số lượng cho productDetail
            productDetailRepository.save(productDetail);
        }

        cartDetailRepositoty.deleteAll(listCartDetails); // xóa tất cả item trong cart

        cartRepository.delete(cart);
    }

    public Cart applyDiscountToCart(Integer cartId, Integer discountId) throws  Exception{
        Cart cart = cartRepository.findByIdCart(cartId);
        Discount discount = discountService.findDiscountById(discountId);

        if (cart == null || discount == null || discount.getUsageLimit() <= 0) {
            throw new Exception("Không thể áp dụng mã giảm giá");
        }

        Discount currentDiscount = cart.getDiscount();
        if (currentDiscount != null) {
            currentDiscount.setUsageLimit(currentDiscount.getUsageLimit() + 1); // hoàn lại lượt
            discountService.saveDiscount_Cart(currentDiscount); // lưu lại mã cũ
        }

        BigDecimal totalPriceCart = cart.getTotal_price_cart();
        if (totalPriceCart == null){
            totalPriceCart = BigDecimal.ZERO;
        }

        BigDecimal discountValue = discount.getDiscountValue();
        if (discountValue == null) {
            discountValue = BigDecimal.ZERO;
        }

        BigDecimal totalDiscount;

        if (discount.getDiscountType().equals("amount")) {
            totalDiscount = discountValue;

            // Giảm không vượt quá maxDiscount nếu có
            BigDecimal maxDiscount = discount.getMaxDiscount();
            if (maxDiscount != null && totalDiscount.compareTo(maxDiscount) > 0) {
                totalDiscount = maxDiscount;
            }
        } else if (discount.getDiscountType().equals("percent")) {
            totalDiscount = totalPriceCart.multiply(discountValue).divide(BigDecimal.valueOf(100));

            // Giảm không vượt quá maxDiscount nếu có
            BigDecimal maxDiscount = discount.getMaxDiscount();
            if (maxDiscount != null && totalDiscount.compareTo(maxDiscount) > 0) {
                totalDiscount = maxDiscount;
            }
        } else {
            throw new Exception("Loại mã giảm giá không hợp lệ");
        }

        // Đảm bảo không giảm quá tổng tiền -> tổng discount không âm
        if (totalDiscount.compareTo(totalPriceCart) > 0) {
            totalDiscount = totalPriceCart;
        }

        cart.setTotal_discount(totalDiscount);
        cart.setTotal_price_checkout(totalPriceCart.subtract(totalDiscount));

        cart.setDiscount(discount);
        cartRepository.save(cart);

        discount.setUsageLimit(discount.getUsageLimit() - 1);
        discountService.saveDiscount_Cart(discount);
        return cart;
    }

    public void removeDiscountFromCart(Integer cartId) throws Exception {
        Cart cart = cartRepository.findByIdCart(cartId);
        Discount discount = cart.getDiscount();

        if (cart == null || discount == null) {
            return;
        }

        discount.setUsageLimit(discount.getUsageLimit() + 1);
        discountService.saveDiscount_Cart(discount);

        // Xoá discount khỏi cart
        cart.setDiscount(null);
        cart.setTotal_discount(BigDecimal.ZERO);
        cart.setTotal_price_checkout(cart.getTotal_price_cart());
        cart.setUpdated_at(new Date());
        cartRepository.save(cart);
    }

    //Tính toán lại tiền và giá giảm
    public void recalculateCartTotalWithDiscount(Cart cart) {
        BigDecimal totalPrice = cart.getTotal_price_cart();
        Discount discount = cart.getDiscount();

        if (discount != null && discount.getUsageLimit() > 0) {
            BigDecimal discountValue = discount.getDiscountValue() != null ? discount.getDiscountValue() : BigDecimal.ZERO;

            if ("percent".equalsIgnoreCase(discount.getDiscountType())) {
                BigDecimal discountAmount = totalPrice.multiply(discountValue).divide(BigDecimal.valueOf(100));
                BigDecimal maxDiscount = discount.getMaxDiscount() != null ? discount.getMaxDiscount() : BigDecimal.ZERO;

                if (discountAmount.compareTo(maxDiscount) > 0) {
                    discountAmount = maxDiscount;
                }
                cart.setTotal_discount(discountAmount);

            } else { // amount
                cart.setTotal_discount(discountValue);
            }

            // Trường hợp tổng tiền bị giảm hết
            if (cart.getTotal_discount().compareTo(totalPrice) >= 0) {
                cart.setTotal_price_checkout(BigDecimal.ZERO);
            } else {
                cart.setTotal_price_checkout(totalPrice.subtract(cart.getTotal_discount()));
            }

        } else {
            cart.setTotal_discount(BigDecimal.ZERO);
            cart.setTotal_price_checkout(totalPrice);
        }
    }

// <<<<<<< TienBB
//     public void addCustomerToCart(Integer cartId, Integer customerId) throws Exception {
//         Cart cart = cartRepository.findByIdCart(cartId);
//         if (cart == null) {
//             throw new Exception("Không tìm thấy giỏ hàng");
//         }

//         Customer customer = customerService.findById(customerId);
//         if (customer == null) {
//             throw new Exception("Không tìm thấy khách hàng");
//         }

//         cart.setAccount(customer.getAccount());
//         cartRepository.save(cart);
//     }

//     public void removeCustomerFromCart(Integer cartId) throws Exception {
//         Cart cart = cartRepository.findByIdCart(cartId);
//         if (cart == null) {
//             throw new Exception("Không tìm thấy giỏ hàng");
//         }

//         cart.setAccount(null);
// =======
    // ban hang online ne ca nhom :D

    public void addProductOnlineToCart(Integer cartId, Integer productDetailId, Integer quantity, Account account) throws Exception {
        // Lấy thông tin giỏ hàng
        Cart cart = cartRepository.findByIdCart(cartId);
        if (cart == null) {
            throw new Exception("Giỏ hàng không tồn tại");
        }

        ProductDetail productDetail = productDetailRepository.findProductDetailById(productDetailId);
        if (productDetail == null) {
            throw new Exception("Sản phẩm không tồn tại");
        }

        if (productDetail.getQuantity() < quantity) {
            throw new Exception("Sản phẩm " + productDetail.getProduct().getName() +
                    " (" + productDetail.getColor().getName() + " - " + productDetail.getSize().getCode() + ")" +
                    " không đủ hàng (tồn kho: " + productDetail.getQuantity() + ")");
        }

        CartDetail existingItem = cartDetailRepositoty.findByCartAndProductDetailId(cartId, productDetailId);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
            existingItem.setTotal_price(productDetail.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            cartDetailRepositoty.save(existingItem);
        } else {
            CartDetail newItem = new CartDetail();
            newItem.setCart(cart);
            newItem.setProductDetail(productDetail);
            newItem.setQuantity(quantity);
            newItem.setPrice(productDetail.getPrice());
            newItem.setTotal_price(productDetail.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cartDetailRepositoty.save(newItem);
        }

        List<CartDetail> cartDetails = cartRepository.findAllCartDetailByCartId(cartId);
        BigDecimal totalPrice = cartDetails.stream()
                .map(CartDetail::getTotal_price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalQuantity = cartDetails.stream()
                .mapToInt(CartDetail::getQuantity)
                .sum();

        cart.setTotal_price_cart(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        if (cart.getAccount() == null && account != null) {
            cart.setAccount(account);
        }

        recalculateCartTotalWithDiscount(cart);

        cart.setUpdated_at(new Date());

        cartRepository.save(cart);
    }


    public Integer findDiscountIdByCode(String code) {
        Optional<Discount> optionalDiscount = discountService.findByCode(code.trim());

        if (optionalDiscount.isEmpty()) {
            return null;
        }

        Discount discount = optionalDiscount.get();

        if (discount.getUsageLimit() != null && discount.getUsageLimit() <= 0) {
            return null;
        }

        return discount.getId();
    }

}
