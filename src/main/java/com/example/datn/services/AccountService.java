package com.example.datn.services;
import com.example.datn.dto.employee.AccountDto;
import com.example.datn.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    Page<Account> listAccountAndPage(Pageable pageable);

    void add(AccountDto accountDto);

    void update(AccountDto accountDto);

    String generateEmployeeCode();

    String generateRandomPassword(int length);

    Account findByEmail(String email);
}
