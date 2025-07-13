package com.example.datn.services;

import com.example.datn.dto.selling_inline.BillDetailDto;
import com.example.datn.dto.selling_inline.BillSessionDto;
import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import com.example.datn.repositories.BillRepository;
import com.example.datn.repositories.cart.CartRepository;
import com.example.datn.repositories.product_and_other.BillDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    BillDetailsRepository billDetailsRepository;

    @Autowired
    CartRepository cartRepository;
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
        bill.setStatus(1);
        bill.setType_bill(false); // bán tại quầy
        bill.setDeliveryType(0); // 0 = không giao hàng
        bill.setShippingFee(BigDecimal.ZERO);
        bill.setName("tien");
        bill.setPhoneNumber("0365142537");
        bill.setEmail("tien@gmail.com");
        bill.setPaymentMethod(1); // sửa sau, tạm thời fix để bán thử
        bill.setDiscountId(null); // fix cứng
        bill.setCustomer(null);
        bill.setCreated_at(LocalDateTime.now());

        billRepository.save(bill);
        System.out.println("Thong tin bill :" + bill);
        for (CartDetail cartDetail : listCartDetails){
            BillDetails billDetails = new BillDetails();
            billDetails.setPrice(cartDetail.getPrice());
            billDetails.setQuantity(cartDetail.getQuantity());
            billDetails.setBill(bill);
            billDetails.setProductDetail(cartDetail.getProductDetail());
            billDetailsRepository.save(billDetails);
        }

        cart.setStatus(false); // không hiển thị lên list nữa
        cart.setUpdated_at(new Date());
        cartRepository.save(cart);
    }
}
