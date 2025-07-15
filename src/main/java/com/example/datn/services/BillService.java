package com.example.datn.services;


import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Discount;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.BillDetailRepository;
import com.example.datn.repositories.BillRepository;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public Bill findById(Integer id) {
        return billRepository.findById(id).orElse(null);
    }

    public String findLastCodeBill(){
        return billRepository.findMaxCodeBill();
    }

    public String taoMaTuDongBill(){
        String lastCode = findLastCodeBill();
        int nextCode = 1;

        if(lastCode != null && !lastCode.trim().isEmpty()){
            try{
                String numberPart = lastCode.substring(2); // lay so phia sau Hoa don
                nextCode = Integer.parseInt(numberPart) + 1; // cong them 1
            }catch (NumberFormatException e){
    //                hihi
            }
        }
        return String.format("HD%03d",nextCode);
    }

    public void checkOut(Integer cartId) throws Exception{
        Cart cart = cartRepository.findByIdCart(cartId);
        if(cart == null || cart.getStatus() == false){
            throw new Exception("Giỏ hàng không tồn tại hoặc đã được thanh toán");
        }

        List<CartDetail> listCartDetails = cartRepository.findAllCartDetailByCartId(cartId);
        if(listCartDetails == null || listCartDetails.isEmpty()){
            throw new Exception("Không có sản phẩm trong giỏ hàng");
        }
        BigDecimal totalAmount = listCartDetails.stream()
                .map(CartDetail::getTotal_price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Bill bill = new Bill();
        bill.setCode(taoMaTuDongBill());
        bill.setDiscountAmount(BigDecimal.ZERO); // fix cứng
        bill.setTotalAmount(totalAmount);
        bill.setPaymentStatus(true);
        bill.setStatus(4);
        bill.setTypeBill(false); // bán tại quầy
        bill.setDeliveryType(0); // 0 = không giao hàng
        bill.setShippingFee(BigDecimal.ZERO);
        bill.setName("tien");
        bill.setPhoneNumber("0365142537");
        bill.setEmail("tien@gmail.com");
//        bill.setPaymentMethod(1); // sửa sau, tạm thời fix để bán thử
        bill.setDiscount(cart.getDiscount()); // nếu cart đã có discount
        bill.setDiscountAmount(cart.getTotal_discount()); // nếu muốn lưu số tiền đã giảm
        bill.setCustomer(null);
        bill.setCreatedAt(LocalDateTime.now());

        billRepository.save(bill);
        System.out.println("Thong tin bill :" + bill);
        for (CartDetail cartDetail : listCartDetails){
            BillDetails billDetails = new BillDetails();
            billDetails.setPrice(cartDetail.getPrice());
            billDetails.setQuantity(cartDetail.getQuantity());
            billDetails.setBill(bill);
            billDetails.setProductDetail(cartDetail.getProductDetail());
            billDetailRepository.save(billDetails);
        }

        cart.setStatus(false); // không hiển thị lên list nữa
        cart.setUpdated_at(new Date());
        cartRepository.save(cart);
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
}
