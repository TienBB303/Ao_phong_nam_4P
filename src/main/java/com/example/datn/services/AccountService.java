package com.example.datn.services;

import com.example.datn.dto.request.AccountRequestDto;
import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Role;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.RoleRepository;
import org.hibernate.ResourceClosedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public Page<Account> getAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }


    public Account createNewAccount(AccountRequestDto accountRequestDto) {
        if (accountRepository.existsByEmail(accountRequestDto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (accountRepository.existsByPhoneNumber(accountRequestDto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }
        Account account = new Account();

        account.setCode(generateNextAccountCode());
        account.setFullName(accountRequestDto.getFullName());
        account.setPassword(accountRequestDto.getPassword());
        account.setPhoneNumber(accountRequestDto.getPhoneNumber());
        account.setEmail(accountRequestDto.getEmail());
        account.setAddressDetail(accountRequestDto.getAddressDetail());
        account.setCreated_at(LocalDateTime.now());
        account.setBirthOfDate(accountRequestDto.getBirthOfDate());
        account.setGender(accountRequestDto.getGender());
        account.setStatus(Boolean.TRUE);

        Role role = roleRepository.findByName("ROLE_EMPLOYEE").orElseThrow(() -> new RuntimeException("Khong tim thay quyen hop le!"));
        account.setRole(role);

        return accountRepository.save(account);
    }

    private String generateNextAccountCode() {
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

    public Account getDetail(Integer id) {
        return accountRepository.findById(id).orElse(null);
    }


    public Account update(Account a) {
        a.setUpdated_at(LocalDateTime.now());
        return accountRepository.save(a);
    }
}
