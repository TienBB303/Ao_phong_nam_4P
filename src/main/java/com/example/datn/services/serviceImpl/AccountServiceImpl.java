package com.example.datn.services.serviceImpl;

import com.example.datn.dto.employee.AccountDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Role;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.ShippingAddress;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.repositories.RoleRepository;
import com.example.datn.repositories.ShippingAddressRepository;
import com.example.datn.services.AccountService;
import com.example.datn.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<Account> listAccountAndPage(Pageable pageable) {

//        Page<Account> all = accountRepository.findAll(pageable);
//        // Lọc bỏ admin khỏi danh sách
//        List<Account> filtered = all.getContent().stream()
//                .filter(a -> a.getRole() != null && !"ROLE_ADMIN".equals(a.getRole().getName()))
//                .toList();
//        return new org.springframework.data.domain.PageImpl<>(filtered, pageable, all.getTotalElements());
        // Danh sách nhân viên: chỉ lấy ROLE_EMPLOYEE
        return accountRepository.findByRole_Name("ROLE_EMPLOYEE", pageable);
    }
    @Override
    public Page<Account> listAccountsByRole(String roleName, Pageable pageable) {
        return accountRepository.findByRole_Name(roleName, pageable);
    }
    @Override
    public Page<Account> listAccountsExcludingRole(String roleName, Pageable pageable) {
        return accountRepository.findByRole_NameNot(roleName, pageable);

//         return accountRepository.findByCodeStartingWith("NV", pageable);

    }

    @Override
    public void add(AccountDto accountDto) {
        Customer customerCreatePart1 = new Customer();

        customerCreatePart1.setName(accountDto.getFullName());
        customerCreatePart1.setPhoneNumber(accountDto.getPhoneNumber());
        customerCreatePart1.setBirthDate(accountDto.getBirthDate());
        customerCreatePart1.setGender(accountDto.getGender());
        customerCreatePart1.setIsActive(true);
        customerCreatePart1 = customerRepository.save(customerCreatePart1);

        ShippingAddress address = new ShippingAddress();
        address.setCustomer(customerCreatePart1);
        address.setProvinceId(accountDto.getAddress().getProvinceId());
        address.setProvinceName(accountDto.getAddress().getProvinceName());
        address.setDistrictId(accountDto.getAddress().getDistrictId());
        address.setDistrictName(accountDto.getAddress().getDistrictName());
        address.setWardId(accountDto.getAddress().getWardId());
        address.setWardName(accountDto.getAddress().getWardName());
        address.setAddressDetail(accountDto.getAddress().getAddressDetail());
        address.setIsDefault(true);
        shippingAddressRepository.save(address);

        Account account = new Account();
        String rawPassword = generateRandomPassword(8);
        account.setCode(generateEmployeeCode());
        account.setEmail(accountDto.getEmail());
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setCreatedAt(LocalDateTime.now());
        account.setStatus(accountDto.getStatus());
        account.setCustomer(customerCreatePart1);

        Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Quyen khong ton tai, vui long thu lai!"));
        account.setRole(role);

        accountRepository.save(account);

        // send email created account successfully
        try {
            emailService.sendAccountCreatedMail(accountDto.getEmail(), accountDto.getFullName(),
                    accountDto.getEmail(), rawPassword);
        } catch (Exception e) {
            System.err.println("Email gui that bai: " + e.getMessage());
        }
    }

    @Override
    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found with email: " + email));
    }


    @Override
    public void update(AccountDto accountDto) {
        if (accountDto == null || accountDto.getId() == null) {
            throw new RuntimeException("ID nhân viên không hợp lệ");
        }
        Account account = accountRepository.findById(accountDto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + accountDto.getId()));

        // Cập nhật thông tin tài khoản
        if (accountDto.getEmail() != null && !accountDto.getEmail().isBlank()) {
            String newEmail = accountDto.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(account.getEmail()) && accountRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email đã tồn tại");
            }
            account.setEmail(newEmail);
        }
        if (accountDto.getStatus() != null) {
            account.setStatus(accountDto.getStatus());
        }
        account.setUpdatedAt(java.time.LocalDateTime.now());

        // Cập nhật thông tin khách hàng liên kết
        Customer customer = account.getCustomer();
        if (customer == null) {
            customer = new Customer();
            customer.setIsActive(true);
            customer = customerRepository.save(customer);
            account.setCustomer(customer);
        }
        if (accountDto.getFullName() != null && !accountDto.getFullName().isBlank()) {
            customer.setName(accountDto.getFullName());
        }
        if (accountDto.getPhoneNumber() != null && !accountDto.getPhoneNumber().isBlank()) {
            customer.setPhoneNumber(accountDto.getPhoneNumber());
        }
        if (accountDto.getBirthDate() != null) {
            customer.setBirthDate(accountDto.getBirthDate());
        }
        if (accountDto.getGender() != null) {
            customer.setGender(accountDto.getGender());
        }
        customerRepository.save(customer);

        // Cập nhật địa chỉ mặc định của khách nếu có DTO
        com.example.datn.dto.AddressDto addressDto = accountDto.getAddress();
        if (addressDto != null) {
            ShippingAddress address = null;
            if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
                address = customer.getAddresses().stream()
                        .filter(a -> java.util.Objects.equals(Boolean.TRUE, a.getIsDefault()))
                        .findFirst()
                        .orElse(customer.getAddresses().get(0));
            }
            if (address == null) {
                address = new ShippingAddress();
                address.setCustomer(customer);
            }
            if (addressDto.getAddressDetail() != null && !addressDto.getAddressDetail().isBlank()) {
                address.setAddressDetail(addressDto.getAddressDetail());
            }
            if (addressDto.getProvinceId() != null) {
                address.setProvinceId(addressDto.getProvinceId());
                address.setProvinceName(addressDto.getProvinceName());
            }
            if (addressDto.getDistrictId() != null) {
                address.setDistrictId(addressDto.getDistrictId());
                address.setDistrictName(addressDto.getDistrictName());
            }
            if (addressDto.getWardId() != null && !addressDto.getWardId().isBlank()) {
                address.setWardId(addressDto.getWardId());
                address.setWardName(addressDto.getWardName());
            }
            if (addressDto.getReceiverName() != null && !addressDto.getReceiverName().isBlank()) {
                address.setReceiverName(addressDto.getReceiverName());
            }
            if (addressDto.getReceiverPhoneNumber() != null && !addressDto.getReceiverPhoneNumber().isBlank()) {
                address.setReceiverPhoneNumber(addressDto.getReceiverPhoneNumber());
            }
            if (addressDto.getIsDefault() != null) {
                boolean isDefault = addressDto.getIsDefault();
                address.setIsDefault(isDefault);
                if (isDefault) {
                    shippingAddressRepository.updateAllDefaultFalseByCustomerId(customer.getId());
                    address.setIsDefault(true);
                }
            }
            shippingAddressRepository.save(address);
        }

        accountRepository.save(account);
    }

    @Override
    public String generateEmployeeCode() {
        Optional<Account> lastAccountOpt = accountRepository.findTopByOrderByCodeDesc();
        if (lastAccountOpt.isPresent()) {
            String codeAuto = lastAccountOpt.get().getCode();
            if (codeAuto != null && codeAuto.startsWith("NV")) {
                try {
                    int number = Integer.parseInt(codeAuto.substring(2));
                    return String.format("NV%03d", number + 1);
                } catch (NumberFormatException e) {
                    return "NV001";
                }
            }
        }
        return "NV001";
    }

    public String generateRandomPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$%!";

        String allChars = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Bắt buộc có ít nhất 1 mỗi loại
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Thêm ngẫu nhiên các ký tự còn lại
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Xáo trộn vị trí các ký tự
        List<Character> chars = password.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(chars);
        return chars.stream().map(String::valueOf).collect(Collectors.joining());
    }


    public Account loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));
    }


    @Override
    public Cart getCartByAccountID(Integer accountId) {
        return accountRepository.findByAccountID(accountId);
    }

    @Override
    public Account findById(Integer id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
    }

}
