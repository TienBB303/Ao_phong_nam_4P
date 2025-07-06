package com.example.datn.services;

//import com.example.datn.dto.customer.CustomerDto;

import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    //check emai trùng
    boolean isEmailExists(String email);
    // Lấy danh sách khách hàng đang hoạt động
    Page<Customer> getAllCustomersEntity(Pageable pageable);

    //    CustomerDto createCustomerAdmin(CustomerDto customerDto);
    Customer findById(int id);

    // Tìm kiếm khách hàng theo tên hoặc mã
    Page<Customer> searchCustomerEntity(String keyword, Pageable pageable);

    // Tạo khách hàng từ entity
    Customer createCustomerEntity(Customer customer);

    // Xóa mềm
    void softDeleteCustomer(Integer id);

    // Cập nhật khách hàng
    Customer updateCustomer(CustomerDto dto);

    // Tạo khách hàng từ DTO (dùng cho controller)
    void createCustomer(CustomerDto dto);
}
