package com.example.datn.controllers;

import com.example.datn.dto.request.AccountRequestDto;
import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import com.example.datn.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountRequestDto accountRequestDto,
                                           BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }

        accountService.createNewAccount(accountRequestDto);
        return ResponseEntity.ok("Thêm mới thành công!");
    }

    @GetMapping("/detail")
    public List<AccountResponseDto> detail(@RequestParam String code) {
        return accountService.getDetail(code);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateAccount(@Valid @RequestBody AccountRequestDto accountRequestDto,
                                           BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            accountService.updateAccount(accountRequestDto);
            return ResponseEntity.ok("Cập nhật thành công!");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống!");
        }
    }


}
