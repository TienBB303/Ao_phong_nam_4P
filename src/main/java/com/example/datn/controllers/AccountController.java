package com.example.datn.controllers;

import com.example.datn.dto.request.AccountRequestDto;
import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/employee")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String index(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> pagedList = accountService.getAll(pageable);

        model.addAttribute("listEmployee", pagedList.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedList.getTotalPages());

        return "admin/employee/index";
    }

    @GetMapping("/add")
    public String addShow() {

        return "admin/employee/add";
    }

    @PostMapping("/add")
   public String add(@ModelAttribute AccountRequestDto accountRequestDto) {
        accountService.createNewAccount(accountRequestDto);

        return "redirect:/admin/employee";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam Integer id, Model m) {
        m.addAttribute("detail", accountService.getDetail(id));

        return "admin/employee/detail";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Account a) {
        accountService.update(a);

        return "redirect:/admin/employee";
    }
}
