package com.example.datn.services;

//import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<Customer> getAllCustomersEntity(Pageable pageable);

//    CustomerDto createCustomerAdmin(CustomerDto customerDto);

    Page<Customer> searchCustomerEntity(String keyword, Pageable pageable);
    Customer createCustomerEntity(Customer customer);
    void softDeleteCustomer(Integer id);

}
