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
        return accountRepository.findByCodeStartingWith("NV", pageable);
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
