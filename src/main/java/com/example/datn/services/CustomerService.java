package com.example.datn.services;

//import com.example.datn.dto.customer.CustomerDto;

import com.example.datn.dto.AddressDto;
import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.ShippingAddress;
import com.example.datn.entities.Customer;
import com.example.datn.entities.ShippingAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CustomerService {
    //check emai trùng
    boolean isEmailExists(String email);
    //check sdt trùng
    boolean isPhoneNumberExists(String phoneNumber);
    // Lấy danh sách khách hàng đang hoạt động (isActive = true)
    Page<Customer> getAllCustomersEntity(Pageable pageable);
    // Tìm kiếm khách hàng theo tên hoặc mã
    Page<Customer> searchCustomerEntity(String keyword, Pageable pageable);

    // ✅ Tạo khách hàng từ DTO, kèm tạo tài khoản, địa chỉ và gửi email đăng nhập
    Customer createCustomerWithAddressAndAccount(CustomerDto dto);
    // Tạo khách hàng từ DTO + địa chỉ (dành cho admin không cần gửi mail)
    Customer createCustomerWithAddress(CustomerDto dto);

    Customer createCustomerEntity(Customer customer);
    // ✅ Tìm khách hàng theo ID
    Customer findById(int id);

    Customer findByIdWithAddresses(Integer id);

    // ✅ Thêm method fetch-join cả addresses và account để đảm bảo dữ liệu cho trang edit
    Customer findByIdWithAddressesAndAccount(Integer id);

    // ✅ Cập nhật thông tin khách hàng
    Customer updateCustomer(CustomerDto dto);

    // ✅ Xóa mềm khách hàng (set isActive = false)
    void softDeleteCustomer(Integer id);

    // Thêm địa chỉ cho khách hàng
    ShippingAddress createAddressForCustomer(Integer customerId, AddressDto addressDto);

    // Đếm tổng số khách hàng (phục vụ phân trang)
    long countAllCustomers();
    List<Customer> searchCustomerInline(String keyword);

    Customer createCustomerInline(com.example.datn.dto.selling_inline.CustomerDto dto) throws Exception;

    // Các phương thức cho user authentication
    boolean existsByEmail(String email);
    com.example.datn.entities.Account authenticateUser(String email, String password);
    boolean verifyPassword(String rawPassword, String encodedPassword);
    void updatePassword(Integer accountId, String newPassword);
    void updateAccountStatus(Integer accountId, boolean status);
    // Cập nhật thông tin cá nhân cho user
    void updateCustomerProfile(Integer customerId, CustomerDto dto);

    com.example.datn.entities.Account findAccountById(Integer accountId);

    Account findAccountByCustomerID(Integer id);
}