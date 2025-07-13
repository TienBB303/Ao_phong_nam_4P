package com.example.datn.services;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.cart.CartDetailRepositoty;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartDetailRepositoty cartDetailRepositoty;

    @Autowired
    ProductDetailRepository productDetailRepository;

    public List<Cart> getAllCarts(){
        return cartRepository.findAll();
    }

    public Cart SaveCart(Cart cart){
        return cartRepository.save(cart);
    }

    public Cart findCartById(Integer idCart){
        return cartRepository.findByIdCart(idCart);
    }
    public CartDetail findCartDetailById(Integer idCartDetail){
        return cartDetailRepositoty.findCartDetailById(idCartDetail);
    }

    public List<CartDetail> findAllCartDetailsByCartId(Integer idCart){
        return  cartRepository.findAllCartDetailByCartId(idCart);
    }

    public void addProductToCart(Integer cartId,Integer productDetailId) throws Exception{
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

}
