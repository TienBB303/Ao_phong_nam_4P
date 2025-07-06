package com.example.datn.services.serviceImpl;

import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.services.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Tạo mã khách hàng tự động
    private String generateCustomerCode() {
        Customer lastCustomer = customerRepository.findTopByOrderByIdDesc();
        int nextId = (lastCustomer == null) ? 1 : lastCustomer.getId() + 1;
        return String.format("KH%04d", nextId); // KH0001, KH0002, ...
    }

    // Lấy danh sách tất cả khách hàng đang hoạt động
    @Override
    public Page<Customer> getAllCustomersEntity(Pageable pageable) {
        return customerRepository.findByIsActiveTrue(pageable);
    }

    // Tìm kiếm khách hàng theo keyword (name/code) và đang hoạt động
    @Override
    public Page<Customer> searchCustomerEntity(String keyword, Pageable pageable) {
        return customerRepository.searchCustomerKeyword(keyword, pageable);
    }

    // Tạo khách hàng mới từ entity
    @Override
    public Customer createCustomerEntity(Customer customer) {
        if (customer.getCode() == null || customer.getCode().trim().isEmpty()) {
            customer.setCode(generateCustomerCode());
        }
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }
        return customerRepository.save(customer);
    }

    // Tạo khách hàng mới từ DTO
    @Override
    public void createCustomer(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setCode(generateCustomerCode());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setIsActive(true);

        customerRepository.save(customer);
    }

    // Xóa mềm khách hàng
    @Override
    public void softDeleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id = " + id));
        customer.setIsActive(false);
        customerRepository.save(customer);
    }

    // Cập nhật thông tin khách hàng
    @Override
    public Customer updateCustomer(Integer id, Customer customer) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id = " + id));

        existing.setName(customer.getName());
        existing.setPhoneNumber(customer.getPhoneNumber());
        existing.setEmail(customer.getEmail());
        existing.setAddress(customer.getAddress());

        return customerRepository.save(existing);
    }
    @Override
    public Customer findById(int id) {
        return customerRepository.findById(id).orElse(null);
    }
    @Override
    public boolean isEmailExists(String email) {
        return customerRepository.existsByEmail(email);
    }

}
