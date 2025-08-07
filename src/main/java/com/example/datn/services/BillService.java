package com.example.datn.services;


import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Discount;
import com.example.datn.entities.PaymentMethod;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.BillDetailRepository;
import com.example.datn.repositories.BillRepository;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BillService {
    @Autowired
    BillRepository billRepository;

    @Autowired
    BillDetailRepository billDetailRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductDetailRepository productDetailRepository;

    @Autowired
    DiscountService discountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    PaymentMethodService paymentMethodService;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    com.example.datn.repositories.PaymentMethodRepository paymentMethodRepository;

//
//    @Autowired
//    CustomerService customerService;
//
//    public Bill saveBill(BillSessionDto billSessionDto){
//        Bill bill = new Bill();
//        bill.setCode(billSessionDto.getBillDto().getCode());
//        bill.setName(billSessionDto.getBillDto().getName());
//        bill.setDiscountAmount(billSessionDto.getBillDto().getDiscountAmount());
//        bill.setTotalAmount(billSessionDto.getBillDto().getTotalAmount());
//        bill.setPaymentStatus(billSessionDto.getBillDto().getPaymentStatus());
//        bill.setStatus(billSessionDto.getBillDto().getStatus());
//        bill.setDeliveryType(billSessionDto.getBillDto().getDeliveryType());
//        bill.setShippingFee(billSessionDto.getBillDto().getShippingFee());
//        bill.setName(billSessionDto.getBillDto().getName());
//        bill.setPhoneNumber(billSessionDto.getBillDto().getPhoneNumber());
//        bill.setEmail(billSessionDto.getBillDto().getEmail());
//        bill.setPaymentMethod(billSessionDto.getBillDto().getPaymentMethodId());
//        bill.setCustomer(customerService.findById(billSessionDto.getBillDto().getCustomerId()));
//        bill.setDiscountId(billSessionDto.getBillDto().getDiscountId());
//
//        return billRepository.save(bill);
//    }

    public Page<Bill> getAllBills(Pageable pageable) {
        return billRepository.findAll(pageable);
    }

    public Page<Bill> searchBills(String code, String name, String phoneNumber,
                                  LocalDateTime startDate, LocalDateTime endDate,
                                  Integer status, Boolean typeBill, Pageable pageable) {
        return billRepository.filterBills(code, name, phoneNumber, startDate, endDate, status, typeBill, pageable);
    }

    public Bill findByCodeAndTypeBill(String code, Boolean typeBill) {

        Bill bill = billRepository.findByCodeWithAllDetailsAndTypeBill(code, typeBill);

        if (bill != null && bill.getBillDetails() != null) {
            for (BillDetails detail : bill.getBillDetails()) {
                if (detail.getProductDetail() != null) {
                    detail.getProductDetail().getProduct().getName();
                    detail.getProductDetail().getColor().getName();
                    detail.getProductDetail().getSize().getName();
                }
            }
        }
        return bill;
    }
    public Bill getOne(Integer id) {
        return billRepository.findById(id).orElse(null);
    }

    public Bill findById(Integer id) {
        return billRepository.findById(id).orElse(null);
    }

    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    public Bill findCartById(Integer idCart) {
        return billRepository.findByIdBill(idCart);
    }

    public List<BillDetails> findAllCartDetailsByCartId(Integer idCart) {
        return billRepository.findAllCartDetailByCartId(idCart);
    }

    public List<Bill> getAllCarts() {
        return billRepository.findAll();
    }

    public List<Bill> getAllCartInline() {
        return billRepository.getAllCartInline();
    }

    public BillDetails findCartDetailById(Integer idCartDetail) {
        return billDetailRepository.findCartDetailById(idCartDetail);
    }

    public Bill findCartByCartDetailId(Integer idCartDetail) {
        return billRepository.findCartByCartDetailId(idCartDetail);
    }

    // đếm tất cả sản phẩm trong cart
    public Integer countItemInCartByCartId(Integer idCart) {
        return billDetailRepository.countItemInCartByCartId(idCart);
    }

    // cộng tổng tất cả sp ytrong cart
    public Integer countAllItemInCartByCartId(Integer idCart) {
        return billDetailRepository.countAllItemInCartByCartId(idCart);
    }

    // cộng tổng tiền trong cart
    public BigDecimal plusAllItemInCartByCartId(Integer idCart) {
        Bill cart = billRepository.findByIdBill(idCart);
        List<BillDetails> listCartDetails = billRepository.findAllCartDetailByCartId(cart.getId());
        BigDecimal totalPriceInCart = BigDecimal.valueOf(0);
        for (BillDetails pd : listCartDetails) {
            totalPriceInCart = totalPriceInCart.add(pd.getTotal_price());
        }
        return totalPriceInCart;
    }

    // thêm sp vào giỏ
    public void addProductToCart(Integer cartId, Integer productDetailId) throws Exception {
        Bill cart = billRepository.findBillById(cartId);
        ProductDetail productDetail = productDetailRepository.findProductDetailById(productDetailId);
        BillDetails itemExisted = billDetailRepository.findByCartAndProductDetailId(cartId, productDetailId);

        if (productDetail.getQuantity() <= 0) {
            throw new Exception("Sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" + " đã hết hàng");
        }
        if (itemExisted != null) {
            if (productDetail.getQuantity() < 1) {
                throw new Exception("Sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" + " đã hết hàng");
            }
            itemExisted.setQuantity(itemExisted.getQuantity() + 1);
            itemExisted.setTotal_price(productDetail.getPrice().multiply(BigDecimal.valueOf(itemExisted.getQuantity())));
            productDetail.setQuantity(productDetail.getQuantity() - 1);
            productDetailRepository.save(productDetail);
            billDetailRepository.save(itemExisted);
        } else {
            if (productDetail.getQuantity() < 1) {
                throw new Exception("Sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")" + " đã hết hàng");
            }
            BillDetails cartDetail = new BillDetails();
            cartDetail.setBill(cart);
            cartDetail.setProductDetail(productDetail);
            cartDetail.setQuantity(1);
            cartDetail.setPrice(productDetail.getPrice());
            cartDetail.setTotal_price(productDetail.getPrice());

            productDetail.setQuantity(productDetail.getQuantity() - 1);
            productDetailRepository.save(productDetail);
            billDetailRepository.save(cartDetail);
        }

        List<BillDetails> listCartDetails = billRepository.findAllCartDetailByCartId(cartId);
        BigDecimal totalPrice = listCartDetails.stream().map(BillDetails::getTotal_price).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalQuantity = listCartDetails.stream().mapToInt(BillDetails::getQuantity).sum();

        cart.setTotalAmount(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        recalculateCart(cart);

        cart.setUpdatedAt(LocalDateTime.now());
        billRepository.save(cart);
    }

    // tăng số lượng trong giỏ
    public void updateQuantityInCart(Integer idCartDetail, Integer newQuantity) throws Exception {
        BillDetails cartDetail = billDetailRepository.findById(idCartDetail).orElseThrow(() -> new Exception("Không tìm thấy giỏ hàng"));

        ProductDetail productDetail = cartDetail.getProductDetail();
        if (newQuantity == null) {
            throw new Exception("Số lượng không để trống");
        }
        if (newQuantity < 1) {
            throw new Exception("Số lượng không được nhỏ hơn 1");
        }
        Integer oldQuantity = cartDetail.getQuantity();
        Integer currentQuantity = productDetail.getQuantity();

        Integer change = newQuantity - oldQuantity; // lay so moi - so luong xu, khong dung tru luon so luong moi, sẽ bị sai
        if (change > 0 && change > currentQuantity) {
            throw new Exception("Số lượng tồn kho không đủ cho " + "sản phẩm " + productDetail.getProduct().getName() + "(" + productDetail.getColor().getName() + "-" + productDetail.getSize().getCode() + ")");
        }
        productDetail.setQuantity(productDetail.getQuantity() - change);
        productDetailRepository.save(productDetail);

        cartDetail.setQuantity(newQuantity);
        cartDetail.setTotal_price(productDetail.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        billDetailRepository.save(cartDetail);

        Bill cart = cartDetail.getBill();
        List<BillDetails> listCartDetails = billRepository.findAllCartDetailByCartId(cart.getId());
        BigDecimal totalPrice = listCartDetails.stream().map(BillDetails::getTotal_price).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalQuantity = listCartDetails.stream().mapToInt(BillDetails::getQuantity).sum();

        cart.setTotalAmount(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        recalculateCart(cart);

        cart.setUpdatedAt(LocalDateTime.now());
        billRepository.save(cart);
    }

    public void deleteItemFromCart(Integer idCartDetail) throws Exception {
        BillDetails cartDetail = billDetailRepository.findById(idCartDetail).orElseThrow(() -> new Exception("Không tìm thấy sản phẩm trong giỏ"));
        if (cartDetail == null) {
            throw new Exception("Không tìm thấy sản phẩm trong giỏ");
        }

        ProductDetail productDetail = cartDetail.getProductDetail();
        productDetail.setQuantity(productDetail.getQuantity() + cartDetail.getQuantity()); // cập nhaarth lại sl
        productDetailRepository.save(productDetail);

        Bill cart = cartDetail.getBill();
        billDetailRepository.delete(cartDetail);

        List<BillDetails> listCartDetails = billRepository.findAllCartDetailByCartId(cart.getId());
        BigDecimal totalPrice = listCartDetails.stream().map(BillDetails::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer totalQuantity = listCartDetails.stream().mapToInt(BillDetails::getQuantity).sum();

        cart.setTotalAmount(totalPrice);
        cart.setTotal_quantity(totalQuantity);

        recalculateCart(cart);

        cart.setUpdatedAt(LocalDateTime.now());
        billRepository.save(cart);
    }

    //xóa giỏ
    public void deleteCart(Integer cartId) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        if (cart == null) {
            throw new Exception("Không tồn tại giỏ hàng");
        }

        List<BillDetails> listCartDetails = billRepository.findAllCartDetailByCartId(cartId);
        for (BillDetails cartDetail : listCartDetails) {
            ProductDetail productDetail = cartDetail.getProductDetail();
            productDetail.setQuantity(productDetail.getQuantity() + cartDetail.getQuantity()); //hoàn lại số lượng cho productDetail
            productDetailRepository.save(productDetail);
        }

        Discount discountInCart = cart.getDiscount();
        if (discountInCart != null) {
            discountInCart.setUsageLimit(discountInCart.getUsageLimit() + 1);
            discountService.saveDiscount_Cart(discountInCart);
        }

        billDetailRepository.deleteAll(listCartDetails); // xóa tất cả item trong cart

        billRepository.delete(cart);
    }

    //áp dụng mã giảm
    public void applyDiscountToCart(Integer cartId, Integer discountId) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        Discount discount = discountService.findDiscountById(discountId);

        if (cart == null || discount == null || discount.getUsageLimit() <= 0) {
            throw new Exception("Không thể áp dụng mã giảm giá");
        }

        Discount currentDiscount = cart.getDiscount();
        if (currentDiscount != null) {
            currentDiscount.setUsageLimit(currentDiscount.getUsageLimit() + 1); // hoàn lại lượt
            discountService.saveDiscount_Cart(currentDiscount); // lưu lại mã cũ
        }

//        BigDecimal totalPriceCart = cart.getTotalAmount();
//        if (totalPriceCart == null) {
//            totalPriceCart = BigDecimal.ZERO;
//        }
//
//        BigDecimal discountValue = discount.getDiscountValue();
//        if (discountValue == null) {
//            discountValue = BigDecimal.ZERO;
//        }
//
//        BigDecimal totalDiscount;
//
//        if (discount.getDiscountType().equals("amount")) {
//            totalDiscount = discountValue;
//
//            // Giảm không vượt quá maxDiscount nếu có
//            BigDecimal maxDiscount = discount.getMaxDiscount();
//            if (maxDiscount != null && totalDiscount.compareTo(maxDiscount) > 0) {
//                totalDiscount = maxDiscount;
//            }
//        } else if (discount.getDiscountType().equals("percent")) {
//            totalDiscount = totalPriceCart.multiply(discountValue).divide(BigDecimal.valueOf(100));
//
//            // Giảm không vượt quá maxDiscount nếu có
//            BigDecimal maxDiscount = discount.getMaxDiscount();
//            if (maxDiscount != null && totalDiscount.compareTo(maxDiscount) > 0) {
//                totalDiscount = maxDiscount;
//            }
//        } else {
//            throw new Exception("Loại mã giảm giá không hợp lệ");
//        }
//
//        // Đảm bảo không giảm quá tổng tiền -> tổng discount không âm
//        if (totalDiscount.compareTo(totalPriceCart) > 0) {
//            totalDiscount = totalPriceCart;
//        }
//
//        cart.setDiscountAmount(totalDiscount);
//        cart.setTotal_checkout(totalPriceCart.subtract(totalDiscount));
//
//        cart.setDiscount(discount);
//        billRepository.save(cart);
//
//        discount.setUsageLimit(discount.getUsageLimit() - 1);
//        discountService.saveDiscount_Cart(discount);
        cart.setDiscount(discount);
        discount.setUsageLimit(discount.getUsageLimit() - 1);               // giảm số lượng của mã giảm
        discountService.saveDiscount_Cart(discount);

        recalculateCart(cart);                                              // gọi hàm tính toán lại tiền

        cart.setUpdatedAt(LocalDateTime.now());                             // cập nhật giờ
        billRepository.save(cart);
    }

    //xóa mã giảm khỏi giỏ
    public void removeDiscountFromCart(Integer cartId) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        Discount discount = cart.getDiscount();

        if (cart == null || discount == null) {
            return;
        }

        // Tăng lại số lượng mã đã dùng
        discount.setUsageLimit(discount.getUsageLimit() + 1);
        discountService.saveDiscount_Cart(discount);

        // Xoá discount khỏi cart
        cart.setDiscount(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        recalculateCart(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        billRepository.save(cart);
    }

    //tính toán lại tiền nếu thêm mã  giảm
    public void recalculateCartTotalWithDiscount(Bill cart) {
        BigDecimal totalAmount = cart.getTotalAmount(); // Tổng tiền hàng chưa giảm
        Discount discount = cart.getDiscount();

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null && discount.getUsageLimit() > 0) {
            BigDecimal discountValue = discount.getDiscountValue() != null ? discount.getDiscountValue() : BigDecimal.ZERO;

            if ("percent".equalsIgnoreCase(discount.getDiscountType())) {
                BigDecimal calculated = totalAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
                BigDecimal maxDiscount = discount.getMaxDiscount() != null ? discount.getMaxDiscount() : BigDecimal.ZERO;
                discountAmount = calculated.min(maxDiscount);
            } else { // kiểu amount
                discountAmount = discountValue;
            }
        }

        cart.setDiscountAmount(discountAmount);

        // Tính lại total_checkout = tiền hàng - giảm giá
        BigDecimal checkout = totalAmount.subtract(discountAmount);
        if (checkout.compareTo(BigDecimal.ZERO) < 0) {
            checkout = BigDecimal.ZERO;                                         // giá tiền tổng nhỏ hơn được giảm -> tiền = 0
        }

        cart.setTotal_checkout(checkout);
    }

    public void recalculateCart(Bill cart) throws Exception {
        // Bước 1: Tính lại tổng tiền sau giảm giá
        recalculateCartTotalWithDiscount(cart); // cập nhật discountAmount + total_checkout (chưa có ship)

        BigDecimal shippingFee = cart.getShippingFee();
        BigDecimal finalTotal = cart.getTotal_checkout(); // Lúc này là subtotal

        // Bước 2: Nếu có phí ship thì cộng vào
        if (shippingFee != null) {
            if (shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("Tiền ship không được âm");
            }
            finalTotal = finalTotal.add(shippingFee);
        }

        // Bước 3: Cập nhật lại tổng tiền thực sự
        cart.setTotal_checkout(finalTotal);
        billRepository.save(cart);
    }

    public Customer  addCustomerToCart(Integer cartId, Integer customerId) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        if (cart == null) {
            throw new Exception("Không tìm thấy giỏ hàng");
        }

        Customer customer = customerService.findById(customerId);
        if (customer == null) {
            throw new Exception("Không tìm thấy khách hàng");
        }

        cart.setCustomer(customer);
//        if (customer.getName() != null && !customer.getName().trim().isEmpty()) {
        cart.setName(customer.getName());
//        }
//        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().trim().isEmpty()) {
        cart.setPhoneNumber(customer.getPhoneNumber());
//        }
//        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
        cart.setAddress_shipping(customer.getAddresses().get(0).toString());
//        }
        billRepository.save(cart);
        return customer;
    }

    public void removeCustomerFromCart(Integer cartId) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        if (cart == null) {
            throw new Exception("Không tìm thấy giỏ hàng");
        }

        cart.setCustomer(null);
        billRepository.save(cart);
    }

    public String findLastCodeBill() {
        return billRepository.findMaxCodeBill();
    }

    public String taoMaTuDongBill(){
        List<String> codes = billRepository.findOfflineBillCodes();
        int max = 0;

        for(String code : codes){
            try{
                String numberPart = code.substring(2); // lấy phần sau 'HD'
                if(numberPart.matches("\\d{3}")){ // chỉ nhận đúng HDxxx
                    int number = Integer.parseInt(numberPart);
                    if(number > max){
                        max = number;
                    }
                }
            } catch(Exception e){

            }
        }

        return String.format("HD%03d", max + 1);
    }


    public void checkDiscountBelongToCart(Bill cart) throws Exception {
        Discount discount = cart.getDiscount();
        if (discount == null) return;

        Discount activeDiscount = discountService.findDiscountById(discount.getId());
        if (activeDiscount == null || !activeDiscount.getStatus().equals(1) || activeDiscount.getUsageLimit() <= 0) {
            throw new Exception("Mã giảm giá không còn hợp lệ. Vui lòng chọn lại mã khác.");
        }
    }


    public void checkOut(Integer cartId, String paymentMethodStr) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        if (cart == null || cart.getStatus() != 9) {
            throw new Exception("Giỏ hàng không tồn tại hoặc đã được thanh toán");
        }

        checkDiscountBelongToCart(cart); // kiểm tra discout còn hoạt động không

        List<BillDetails> listCartDetails = billRepository.findAllCartDetailByCartId(cartId);
        if (listCartDetails == null || listCartDetails.isEmpty()) {
            throw new Exception("Không có sản phẩm trong giỏ hàng");
        }

        cart.setPaymentStatus(true);
        cart.setStatus(4);
        cart.setTypeBill(false); // bán tại quầy
        cart.setShippingFee(BigDecimal.ZERO);
        PaymentMethod paymentMethod = paymentMethodService.findByPaymentMethodName(paymentMethodStr);
        cart.setPaymentMethod(paymentMethod);
        cart.setUpdatedAt(LocalDateTime.now());

        if (cart.getDelivery_type() == false) {
            if (cart.getCustomer() == null) {
                cart.setName("Khách lẻ");
            } else {
                cart.setName(cart.getCustomer().getName());
                cart.setPhoneNumber(cart.getCustomer().getPhoneNumber());
            }
        }

        if (cart.getDelivery_type() == true) {
            if (cart.getName() == null || cart.getName().trim().isEmpty() || cart.getName().trim().equals("")) {
                throw new Exception("Giao hàng không được để trống tên khách hàng");
            } else if (cart.getPhoneNumber() == null || cart.getPhoneNumber().isEmpty() || cart.getPhoneNumber().trim().equals("")) {
                throw new Exception("Giao hàng không được để trống số điện thoại");
            } else if (cart.getAddress_shipping() == null || cart.getAddress_shipping().isEmpty() || cart.getAddress_shipping().trim().equals("")) {
                throw new Exception("Giao hàng không được để trống địa chỉ khách hàng");
            } else if (cart.getShippingFee().compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("Tiền ship không được âm");
            }
        }

        billRepository.save(cart);
        System.out.println("Thong tin bill :" + cart);
    }

    //giao hàng
    public void delivery(Integer cartId, boolean isDelivery, String nameD, String phoneD, String addressD, BigDecimal feeD) throws Exception {
        Bill cart = billRepository.findByIdBill(cartId);
        if (cart == null || cart.getStatus() != 9) {
            throw new Exception("Giỏ hàng không tồn tại hoặc đã được thanh toán");
        }

        cart.setDelivery_type(isDelivery);

        if (cart.getDelivery_type() == true) {
//            Customer customer = cart.getCustomer();
//            if (nameD == null || nameD.trim().isEmpty()) {
//                nameD = (customer != null) ? customer.getName() : "";
//            }
//            if (phoneD == null || phoneD.trim().isEmpty()) {
//                phoneD = (customer != null) ? customer.getPhoneNumber() : "";
//            }
//            if (addressD == null || addressD.trim().isEmpty()) {
//                if (customer != null && customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
//                    addressD = customer.getAddresses().get(0).getAddressDetail();
//                } else {
//                    addressD = "";
//                }
//            }
            cart.setName(nameD);
            cart.setPhoneNumber(phoneD);
            cart.setEmail("");
            cart.setAddress_shipping(addressD);
            cart.setShippingFee(feeD);
        } else {
            cart.setName("");
            cart.setPhoneNumber("");
            cart.setEmail("");
            cart.setAddress_shipping("");
            cart.setShippingFee(BigDecimal.ZERO);
        }
        recalculateCart(cart);
        billRepository.save(cart);
    }
    public Bill updateStatus(String statusString, Integer id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn có id: " + id));

        int newStatus;
        try {
            newStatus = Integer.parseInt(statusString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + statusString);
        }

        int currentStatus = bill.getStatus();

        // Nếu chuyển từ CHỜ XÁC NHẬN (1) → ĐÃ XÁC NHẬN (2) thì trừ tồn kho
        if (currentStatus == 1 && newStatus == 2) {
            try {
                deductProductQuantitiesOnStatusChange(id);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Trừ tồn kho thất bại.");
            }
        }

        // Nếu chuyển sang HUỶ (5) từ trạng thái khác CHỜ XÁC NHẬN (1) thì cộng lại tồn kho
        if (newStatus == 5 && currentStatus != 1) {
            List<BillDetails> billDetailsList = billDetailRepository.findByBillId(id);
            for (BillDetails detail : billDetailsList) {
                ProductDetail productDetail = detail.getProductDetail();
                if (productDetail != null) {
                    productDetail.setQuantity(productDetail.getQuantity() + detail.getQuantity());
                }
            }
        }

        bill.setStatus(newStatus);
        bill.setUpdatedAt(LocalDateTime.now());

        return billRepository.save(bill);
    }

    private void deductProductQuantitiesOnStatusChange(Integer billId) {
        List<BillDetails> billDetailsList = billDetailRepository.findByBillId(billId);

        for (BillDetails detail : billDetailsList) {
            ProductDetail productDetail = detail.getProductDetail();
            if (productDetail == null) {
                throw new RuntimeException("Không tìm thấy thông tin sản phẩm chi tiết.");
            }

            int currentQuantity = productDetail.getQuantity();
            int requiredQuantity = detail.getQuantity();

            if (currentQuantity < requiredQuantity) {
                throw new RuntimeException("Sản phẩm ID " + productDetail.getId() + " không đủ số lượng tồn.");
            }

            productDetail.setQuantity(currentQuantity - requiredQuantity);
            productDetailRepository.save(productDetail);
        }
    }

    public List<BillDetails> findBillDetailsByBillId(Integer billId) {
        return billDetailRepository.findByBillId(billId);
    }

    public Bill findByIdWithDiscount(Integer id) {
        return billRepository.findWithDiscountById(id);
    }

    public void saveBillWithDetails(Bill bill, Cart cart) {
        Bill savedBill = billRepository.save(bill);

        for (CartDetail cd : cart.getCartDetails()) {
            BillDetails detail = new BillDetails();
            detail.setBill(savedBill);
            detail.setProductDetail(cd.getProductDetail());
            detail.setQuantity(cd.getQuantity());
            detail.setPrice(cd.getProductDetail().getPrice());
            billDetailRepository.save(detail);
        }
    }
}