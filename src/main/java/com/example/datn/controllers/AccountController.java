package com.example.datn.controllers;

import com.example.datn.dto.request.AccountRequestDto;
import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account/employee")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<AccountResponseDto> getAll() {
        return accountService.getAll();
    }

    @GetMapping("/page")
    public List<AccountResponseDto> pagination(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return accountService.getPaginate(pageable).getContent();
    }

    @PostMapping("/add")
    public ResponseEntity<?> createAccount(@RequestBody AccountRequestDto accountRequestDto) {
        try {
            accountService.createNewAccount(accountRequestDto);
            return ResponseEntity.ok("Thêm mới thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
