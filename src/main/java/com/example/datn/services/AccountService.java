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

    public List<AccountResponseDto> getAll() {
        return accountRepository.listAccountRes();
    }

    public Page<AccountResponseDto> getPaginate(Pageable pageable) {
        return accountRepository.paginate(pageable);
    }

    public Account createNewAccount(AccountRequestDto accountRequestDto) {
        Account account = new Account();

        account.setCode(generateNextAccountCode());
        account.setFullName(accountRequestDto.getFullName());
        account.setPassword(accountRequestDto.getPassword());
        account.setPhoneNumber(accountRequestDto.getPhoneNumber());
        account.setEmail(accountRequestDto.getEmail());
        account.setCreated_at(LocalDateTime.now());
        account.setBirthOfDate(accountRequestDto.getBirthOfDate());
        account.setGender(accountRequestDto.getGender());

        Role role = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow(() -> new RuntimeException("Khong tim thay quyen hop le!"));
        account.setRole(role);

        return accountRepository.save(account);
    }

    private String generateNextAccountCode() {
        Optional<Account> lastAccountOpt = accountRepository.findTopByOrderByCodeDesc();

        if (lastAccountOpt.isPresent()) {
            String codeAuto = lastAccountOpt.get().getCode();

            if (codeAuto != null && codeAuto.startsWith("TK")) {
                try {
                    int number = Integer.parseInt(codeAuto.substring(2));
                    return String.format("TK%03d", number + 1);
                } catch (NumberFormatException e) {
                    return "TK001";
                }
            }
        }
        return "TK001";
    }

    public List<AccountResponseDto> getDetail(String code) {
        return accountRepository.detailByCode(code);
    }

    public Account updateAccount(AccountRequestDto accountRequestDto) {
        Optional<Account> optionalAccount = accountRepository.findById(accountRequestDto.getId());
        if (optionalAccount.isEmpty()) {
            throw new ResourceClosedException("Account not found with ID: " + accountRequestDto.getId());
        }

        Account account = optionalAccount.get();
        account.setFullName(accountRequestDto.getFullName());
        account.setPassword(accountRequestDto.getPassword());
        account.setPhoneNumber(accountRequestDto.getPhoneNumber());
        account.setEmail(accountRequestDto.getEmail());
        account.setUpdated_at(LocalDateTime.now());
        account.setBirthOfDate(accountRequestDto.getBirthOfDate());
        account.setGender(accountRequestDto.getGender());

        return accountRepository.save(account);
    }
}
