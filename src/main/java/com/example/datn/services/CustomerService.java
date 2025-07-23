package com.example.datn.services;

//import com.example.datn.dto.customer.CustomerDto;

import com.example.datn.dto.AddressDto;
import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.ShippingAddress;
import com.example.datn.entities.Customer;
import com.example.datn.entities.ShippingAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // ✅ Cập nhật thông tin khách hàng
    Customer updateCustomer(CustomerDto dto);

    // ✅ Xóa mềm khách hàng (set isActive = false)
    void softDeleteCustomer(Integer id);

// Thêm địa chỉ cho khách hàng
    ShippingAddress createAddressForCustomer(Integer customerId, AddressDto addressDto);

    // Đếm tổng số khách hàng (phục vụ phân trang)
    long countAllCustomers();
}
