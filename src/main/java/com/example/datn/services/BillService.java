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
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    //    ===============================================TIENBB=========================================================================================================
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

    public void addProductDetailToCartByBarcode(Integer cartId, String barcode) throws Exception {
        ProductDetail productDetail = productDetailRepository.findProductDetailsByBarcode(barcode);
        if (productDetail == null) {
            throw new Exception("Không tìm thấy sản phẩm với barcode: " + barcode);
        }
        addProductToCart(cartId, productDetail.getId());
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
        BigDecimal totalPrice = listCartDetails.stream().map(BillDetails::getTotal_price).reduce(BigDecimal.ZERO, BigDecimal::add);
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

    public Customer addCustomerToCart(Integer cartId, Integer customerId) throws Exception {
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
        cart.setName(customer.getName());
        cart.setPhoneNumber(customer.getPhoneNumber());

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

    public String taoMaTuDongBill() {
        List<String> codes = billRepository.findOfflineBillCodes();
        int max = 0;

        for (String code : codes) {
            try {
                String numberPart = code.substring(2); // lấy phần sau 'HD'
                if (numberPart.matches("\\d{3}")) { // chỉ nhận đúng HDxxx
                    int number = Integer.parseInt(numberPart);
                    if (number > max) {
                        max = number;
                    }
                }
            } catch (Exception e) {

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
        if (cart.getDelivery_type() == true) {
            cart.setStatus(1);                              // giao hàng
        } else {
            cart.setStatus(4);                              // không giao hàng
        }
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

    //    ================================Khanh==============================================================================
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

    public void exportInvoiceToResponse(HttpServletResponse response, Bill bill) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Font
        BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(bf, 16, Font.BOLD);
        Font headerFont = new Font(bf, 12, Font.BOLD);
        Font normalFont = new Font(bf, 11, Font.NORMAL);

        // ✅ Header
        Paragraph shopName = new Paragraph("CỬA HÀNG ÁO PHÔNG Nam 4PStore", titleFont);
        shopName.setAlignment(Element.ALIGN_CENTER);
        document.add(shopName);

        Paragraph shopInfo = new Paragraph("Địa chỉ: FPTPolytechnic - Trịnh Văn Bô\nSĐT: 0123 456 789 - Email: contact@4p.vn\n\n", normalFont);
        shopInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(shopInfo);

        // ✅ Thông tin hóa đơn
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Paragraph billInfo = new Paragraph();
        billInfo.setFont(normalFont);
        billInfo.add("Mã hóa đơn: " + bill.getCode() + "\n");
        billInfo.add("Ngày: " + bill.getCreatedAt().format(format) + "\n\n");
        billInfo.add(new Paragraph("Tên khách hàng: " + bill.getName()+ "\n\n"));
        document.add(billInfo);

        // ✅ Bảng chi tiết sản phẩm
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 2, 3, 3});
        table.addCell(new Phrase("STT", headerFont));
        table.addCell(new Phrase("Sản phẩm", headerFont));
        table.addCell(new Phrase("Số lượng", headerFont));
        table.addCell(new Phrase("Đơn giá", headerFont));
        table.addCell(new Phrase("Thành tiền", headerFont));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        int index = 1;
        for (BillDetails detail : bill.getBillDetails()) {
            table.addCell(new Phrase(String.valueOf(index++), normalFont));
            table.addCell(new Phrase(detail.getProductDetail().getProduct().getName(), normalFont));
            table.addCell(new Phrase(String.valueOf(detail.getQuantity()), normalFont));
            table.addCell(new Phrase(formatter.format(detail.getPrice()), normalFont));
            BigDecimal totalLine = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            table.addCell(new Phrase(formatter.format(totalLine), normalFont));
        }
        document.add(table);

        document.add(new Paragraph("\n"));

        // ✅ Tổng kết
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(50);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);

        summary.addCell(new Phrase("Tổng tiền hàng", normalFont));
        summary.addCell(new Phrase(formatCurrency(bill.getTotalAmount()), normalFont));

        BigDecimal discountAmount = bill.getDiscountAmount() != null ? bill.getDiscountAmount() : BigDecimal.ZERO;

        summary.addCell(new Phrase("Giảm giá", normalFont));
        summary.addCell(new Phrase(formatCurrency(discountAmount), normalFont));


        if (bill.getShippingFee() != null) {
            summary.addCell(new Phrase("Phí vận chuyển", normalFont));
            summary.addCell(new Phrase(formatter.format(bill.getShippingFee()), normalFont));
        }

        PdfPCell totalCell = new PdfPCell(new Phrase("TỔNG THANH TOÁN", headerFont));
        totalCell.setColspan(1);
        totalCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        summary.addCell(totalCell);

        PdfPCell totalValue = new PdfPCell(new Phrase(formatCurrency(bill.getTotal_checkout()), headerFont));
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summary.addCell(totalValue);

        document.add(summary);

       // ✅ Footer
        Paragraph footer = new Paragraph("\nXin cảm ơn quý khách!\n", normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

    }
    private String formatCurrency(Object value) {
        if (value == null) {
            return "0 ₫";
        }
        if (value instanceof Number) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(value);
        }
        try {
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                    .format(new BigDecimal(value.toString()));
        } catch (Exception e) {
            return value.toString();
        }
    }

}


