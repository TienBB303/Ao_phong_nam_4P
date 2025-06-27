package com.example.datn.controllers.api;
//import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
import com.example.datn.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/customer")
public class CustomerRestController {
    private final CustomerService customerService;

    @Autowired
    public CustomerRestController(CustomerService customerService) {
        this.customerService = customerService;
    }


    // Lấy tất cả khách hàng có phân trang
    @GetMapping
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerService.getAllCustomersEntity(pageable); // sửa tên method trong service
    }

    // Tìm kiếm khách hàng theo từ khóa
    @GetMapping("/filter")
    public Page<Customer> searchCustomers(@RequestParam String keyword, Pageable pageable) {
        return customerService.searchCustomerEntity(keyword, pageable); // sửa tên method trong service
    }

    // Tạo khách hàng từ JSON
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.createCustomerEntity(customer);
    }

    // Xóa mềm khách hàng
    @DeleteMapping("/{id}")
    public String deleteCustomer(@PathVariable Integer id) {
        customerService.softDeleteCustomer(id);
        return "Xóa khách hàng thành công (soft delete)";
    }
//    @PostMapping("/customer/create")
//    public CustomerDto createCustomerFormData(@ModelAttribute CustomerDto customerDto) {
//        return customerService.createCustomerAdmin(customerDto);
//    }
//    @PostMapping("/api/customer")
//    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
//        return customerService.createCustomerAdmin(customerDto);
//    }
}
