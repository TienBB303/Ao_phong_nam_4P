package com.example.datn.services.serviceImpl;

import com.example.datn.dto.AddressDto;
import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Role;
import com.example.datn.entities.ShippingAddress;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.repositories.RoleRepository;
import com.example.datn.repositories.ShippingAddressRepository;
import com.example.datn.services.CustomerService;
import com.example.datn.services.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Autowired private AccountRepository accountRepository;
    @Autowired private ShippingAddressRepository shippingAddressRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;

    // Tạo mã khách hàng tự động
    private String generateCustomerCode() {
        Customer lastCustomer = customerRepository.findTopByOrderByIdDesc();
        int nextId = (lastCustomer == null) ? 1 : lastCustomer.getId() + 1;
        return String.format("KH%04d", nextId); // KH0001, KH0002, ...
    }
    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
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
        if (isPhoneNumberExists(customer.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại '" + customer.getPhoneNumber() + "' đã tồn tại.");
        }
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }
        return customerRepository.save(customer);
    }
    @Override
    @Transactional
    public Customer createCustomerWithAddress(CustomerDto dto) {
//        if (isEmailExists(dto.getEmail())) {
//            throw new RuntimeException("Email đã tồn tại.");
//        }
        if (isPhoneNumberExists(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại.");
        }

        Customer customer = new Customer();
        customer.setCode(generateCustomerCode());
        customer.setName(dto.getName());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setBirthDate(dto.getBirthday());
        customer.setGender(dto.getGender());
        customer.setIsActive(true);

        Customer savedCustomer = customerRepository.save(customer);

        Account account = new Account();
        account.setCustomer(savedCustomer);
        account.setEmail(dto.getEmail());

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        account.setPassword(encodedPassword);

        accountRepository.save(account);

        if (dto.getAddress() != null) {
            ShippingAddress address = buildShippingAddress(savedCustomer, dto.getAddress());
            shippingAddressRepository.save(address);
        }

        emailService.sendAccountCreatedMail(dto.getEmail(), dto.getName(), dto.getEmail(), rawPassword);

        return savedCustomer;
    }

    @Override
    @Transactional
    public ShippingAddress createAddressForCustomer(Integer customerId, AddressDto addressDto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có ID = " + customerId));

        if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
            shippingAddressRepository.updateAllDefaultFalseByCustomerId(customerId);
        }

        ShippingAddress address = buildShippingAddress(customer, addressDto);

        return shippingAddressRepository.save(address);
    }

    @Override
    public Customer findById(int id) {
        return customerRepository.findById(id).orElse(null);
    }

    // Xóa mềm khách hàng
    @Override
    public void softDeleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id = " + id));
        customer.setIsActive(false);
        customerRepository.save(customer);
    }
    @Override
    public boolean isEmailExists(String email) {
        return accountRepository.existsByEmail(email);
    }
    @Override
    public boolean isPhoneNumberExists(String phoneNumber) {
        return customerRepository.existsByPhoneNumber(phoneNumber);
    }
    // Cập nhật thông tin khách hàng
    @Override
    @Transactional
    public Customer updateCustomer(CustomerDto dto) {
        Customer existing = customerRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id = " + dto.getId()));

        existing.setName(dto.getName());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setIsActive(dto.getIsActive());

        // --- Bổ sung cập nhật địa chỉ ---
        AddressDto addressDto = dto.getAddress();
        if (addressDto != null) {
            // Lấy địa chỉ đầu tiên (hoặc địa chỉ mặc định)
            ShippingAddress address = null;
            if (existing.getAddresses() != null && !existing.getAddresses().isEmpty()) {
                address = existing.getAddresses().get(0); // hoặc tìm địa chỉ mặc định
            }
            if (address == null) {
                // Nếu chưa có địa chỉ, tạo mới
                address = new ShippingAddress();
                address.setCustomer(existing);
            }
            // Cập nhật các trường địa chỉ
            address.setAddressDetail(addressDto.getAddressDetail());
            address.setProvinceId(addressDto.getProvinceId());
            address.setProvinceName(addressDto.getProvinceName());
            address.setDistrictId(addressDto.getDistrictId());
            address.setDistrictName(addressDto.getDistrictName());
            address.setWardId(addressDto.getWardId());
            address.setWardName(addressDto.getWardName());
            address.setReceiverName(addressDto.getReceiverName());
            address.setReceiverPhoneNumber(addressDto.getReceiverPhoneNumber());
            
            // Xử lý checkbox isDefault
            Boolean isDefault = addressDto.getIsDefault();
            if (isDefault == null) {
                isDefault = false;
            }
            address.setIsDefault(isDefault);
            
            // Nếu chọn là mặc định, update các địa chỉ khác về không mặc định
            if (isDefault) {
                shippingAddressRepository.updateAllDefaultFalseByCustomerId(existing.getId());
            }
            
            // Debug log
            System.out.println("Updating address isDefault: " + isDefault);

            shippingAddressRepository.save(address);
        }

        return customerRepository.save(existing);
    }
    private ShippingAddress buildShippingAddress(Customer customer, AddressDto dto) {
        ShippingAddress address = new ShippingAddress();
        address.setCustomer(customer);
        address.setAddressDetail(dto.getAddressDetail());
        address.setProvinceId(dto.getProvinceId());
        address.setDistrictId(dto.getDistrictId());
        address.setWardId(dto.getWardId());
        address.setReceiverPhoneNumber(dto.getReceiverPhoneNumber());
        address.setReceiverName(dto.getReceiverName());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        address.setProvinceName(dto.getProvinceName());
        address.setDistrictName(dto.getDistrictName());
        address.setWardName(dto.getWardName());
        return address;
    }
    @Override
    @Transactional
    public Customer createCustomerWithAddressAndAccount(CustomerDto dto) {
        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại.");
        }
        if (isPhoneNumberExists(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại.");
        }

        Customer customer = new Customer();
        customer.setCode(generateCustomerCode());
        customer.setName(dto.getName());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setBirthDate(dto.getBirthday());
        customer.setGender(dto.getGender());
        customer.setIsActive(true);

        Customer savedCustomer = customerRepository.save(customer);

        // Tạo tài khoản
        Account account = new Account();
        account.setCustomer(savedCustomer);
        account.setEmail(dto.getEmail());

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        account.setPassword(encodedPassword);
        
        // Set role cho khách hàng
        Role role = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_CUSTOMER không tồn tại!"));
        account.setRole(role);
        
        // Set trạng thái ban đầu là false (chưa đổi mật khẩu)
        account.setStatus(false);
        account.setCreatedAt(java.time.LocalDateTime.now());
        
        accountRepository.save(account);

        // Tạo địa chỉ nếu có
        if (dto.getAddress() != null) {
            ShippingAddress address = buildShippingAddress(savedCustomer, dto.getAddress());
            address.setIsDefault(true); // địa chỉ này luôn mặc định
            shippingAddressRepository.save(address);
        }

        // Gửi email thông tin tài khoản
        emailService.sendAccountCreatedMail(dto.getEmail(), dto.getName(), dto.getEmail(), rawPassword);

        return savedCustomer;
    }

    @Override
    public long countAllCustomers() {
        // Nếu chỉ muốn đếm khách hàng đang hoạt động:
        return customerRepository.countByIsActiveTrue();
        // Nếu muốn đếm tất cả khách hàng (kể cả đã bị xóa mềm):
        // return customerRepository.count();
    }

    //TienBB
    @Override
    public List<Customer> searchCustomerInline(String keyword) {
        return customerRepository.searchCustomerByKeywordInline(keyword);
    }

    public Boolean khachHangTonTaiInline( String phoneNumber){
        Customer customer = customerRepository.searchCustomerExistPhoneInline(phoneNumber);
        if(customer != null){
            return true;
        } else {
            return false;
        }
    }
    @Override
    public Customer createCustomerInline(com.example.datn.dto.selling_inline.CustomerDto customerDto) throws Exception{
        Customer customer = new Customer();
        if(khachHangTonTaiInline(customerDto.getPhoneNumber())){
            throw new Exception("Khách hàng đã tồn tại bằng số điện thoại này!");
        }
        if (customerDto.getPhoneNumber().isEmpty() || customerDto.getPhoneNumber().trim().equals("")){
            throw new Exception("Không được để trống số điện thoại khách hàng!");
        }
        if (customerDto.getName().isEmpty() ||customerDto.getName().trim().equals("")){
            throw new Exception("Không được để trống tên khách hàng!");
        }
        String phone = customerDto.getPhoneNumber().replaceAll("\\s+", "");
        if (!phone.trim().matches("^\\d{9,11}$")) {
            throw new Exception("Số điện thoại phải có từ 9 đến 11 chữ số!");
        }
        customer.setCode(generateCustomerCode());
        customer.setName(customerDto.getName().trim());
        customer.setPhoneNumber(phone.trim());
        customer.setIsActive(true);
        return customerRepository.save(customer);
    }

    // Các phương thức cho user authentication
    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public Account authenticateUser(String email, String password) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (passwordEncoder.matches(password, account.getPassword())) {
                return account;
            }
        }
        return null;
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    @Transactional
    public void updatePassword(Integer accountId, String newPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        account.setPassword(encodedPassword);
        account.setUpdatedAt(java.time.LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateAccountStatus(Integer accountId, boolean status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        
        account.setStatus(status);
        account.setUpdatedAt(java.time.LocalDateTime.now());
        accountRepository.save(account);
    }


}
