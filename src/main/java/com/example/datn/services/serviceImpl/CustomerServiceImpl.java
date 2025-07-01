package com.example.datn.services.serviceImpl;

//import com.example.datn.dto.customer.CustomerDto;
//import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
//import com.example.datn.exceptions.ShopApiException;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.services.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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
    // Tạo khách hàng mới
    @Override
    public Customer createCustomerEntity(Customer customer) {
        // Nếu mã khách hàng trống thì tạo mã tự động
        if (customer.getCode() == null || customer.getCode().trim().isEmpty()) {
            Customer lastCustomer = customerRepository.findTopByOrderByIdDesc();
            int nextId = (lastCustomer == null) ? 1 : lastCustomer.getId() + 1;
            customer.setCode(String.format("KH%04d", nextId));
        }
        // Đánh dấu hoạt động mặc định
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }

        return customerRepository.save(customer);
    }
    // Xóa mềm khách hàng
    @Override
    public void softDeleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id = " + id));
        customer.setIsActive(false); // Đánh dấu xóa mềm
        customerRepository.save(customer);
    }
//    @Override
//    public CustomerDto createCustomerAdmin(CustomerDto customerDto) {
//
//        // Sinh mã tự động nếu không truyền vào
//        if (customerDto.getCode() == null || customerDto.getCode().trim().isEmpty()) {
//            Customer lastCustomer = customerRepository.findTopByOrderByIdDesc();
//            int nextId = (lastCustomer == null) ? 1 : lastCustomer.getId() + 1;
//            customerDto.setCode(String.format("KH%04d", nextId));
//        }
//
//        // Kiểm tra mã khách hàng
//        if (customerRepository.existsByCode(customerDto.getCode())) {
//            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã khách hàng đã tồn tại");
//        }
//
//        // Kiểm tra số điện thoại
//        if (customerRepository.existsByPhoneNumber(customerDto.getPhoneNumber())) {
//            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Số điện thoại khách hàng đã tồn tại");
//        }
//        Customer customer = convertToEntity(customerDto);
//        return convertToDto(customerRepository.save(customer));
//    }

//    @Override
//    public Page<CustomerDto> searchCustomerAdmin(String keyword, Pageable pageable) {
//        return customerRepository.searchCustomerKeyword(keyword, pageable);
//    }
//
//
//    private CustomerDto convertToDto(Customer customer) {
//        CustomerDto customerDto = new CustomerDto();
//        customerDto.setId(customer.getId());
//        customerDto.setCode(customer.getCode());
//        customerDto.setName(customer.getName());
//        customerDto.setEmail(customer.getEmail());
//        customerDto.setPhoneNumber(customer.getPhoneNumber());
//        return customerDto;
//    }
//
//    private Customer convertToEntity(CustomerDto customerDto) {
//        Customer customer = new Customer();
//        customer.setCode(customerDto.getCode());
//        customer.setName(customerDto.getName());
//        customer.setEmail(customerDto.getEmail());
////        customer.setAccount(null);
//        //customer.setAddressShippings(null);
//        customer.setPhoneNumber(customerDto.getPhoneNumber());
//        return customer;
//    }

}
