package com.example.datn.controllers;

import com.example.datn.dto.AddressDto;
import com.example.datn.dto.employee.AccountDto;
import com.example.datn.entities.Account;
import com.example.datn.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public String findAccountAndPage(Model m, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 5;

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("code").ascending());
        Page<Account> accountPage = accountService.listAccountAndPage(pageable);

        m.addAttribute("accountPage", accountPage);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", accountPage.getTotalPages());

        return "admin/employee/index";
    }

    @GetMapping("/save")
    public String showFormAdd(Model m) {
        AccountDto accountDto = new AccountDto();
        accountDto.setAddress(new AddressDto());
        m.addAttribute("accountDto", accountDto);
        return "admin/employee/add";
    }

    @PostMapping("/save")
    public String handleAddForm(
            @ModelAttribute("accountDto") AccountDto accountDto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            accountService.add(accountDto);

            redirectAttributes.addFlashAttribute("success", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm nhân viên: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/detail")
    public String detailEmployee(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Account accountDto = accountService.findById(id);
            if (accountDto == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên!");
                return "redirect:/admin/users";
            }
            model.addAttribute("accountDto", accountDto);
            return "admin/employee/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lấy thông tin: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }


}
