package com.example.datn.services;

import com.example.datn.dto.request.AccountRequestDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Role;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    public Page<Account> getAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Account createNewAccount(AccountRequestDto accountRequestDto) {
        if (accountRepository.existsByEmail(accountRequestDto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        Account account = new Account();
        account.setCode(generateNextAccountCode());
        account.setPassword(accountRequestDto.getPassword());
        account.setEmail(accountRequestDto.getEmail());
        account.setCreatedAt(LocalDateTime.now());
        account.setStatus(Boolean.TRUE);
        account.setAvatarUsername(accountRequestDto.getAvatarUsername());
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
        a.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(a);
    }
}
