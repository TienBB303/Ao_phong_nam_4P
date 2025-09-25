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

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.findById(id);
            if (account == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên!");
                return "redirect:/admin/users";
            }
            // Map entity -> dto dùng lại form add.html
            AccountDto dto = new AccountDto();
            dto.setId(account.getId());
            dto.setCode(account.getCode());
            dto.setEmail(account.getEmail());
            dto.setStatus(account.getStatus());
            if (account.getCustomer() != null) {
                dto.setFullName(account.getCustomer().getName());
                dto.setPhoneNumber(account.getCustomer().getPhoneNumber());
                dto.setBirthDate(account.getCustomer().getBirthDate());
                dto.setGender(account.getCustomer().getGender());
                // lấy địa chỉ mặc định nếu có
                if (account.getCustomer().getAddresses() != null && !account.getCustomer().getAddresses().isEmpty()) {
                    var address = account.getCustomer().getAddresses().stream()
                            .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                            .findFirst()
                            .orElse(account.getCustomer().getAddresses().get(0));
                    AddressDto adto = new AddressDto();
                    adto.setAddressDetail(address.getAddressDetail());
                    adto.setProvinceId(address.getProvinceId());
                    adto.setProvinceName(address.getProvinceName());
                    adto.setDistrictId(address.getDistrictId());
                    adto.setDistrictName(address.getDistrictName());
                    adto.setWardId(address.getWardId());
                    adto.setWardName(address.getWardName());
                    adto.setIsDefault(Boolean.TRUE.equals(address.getIsDefault()));
                    dto.setAddress(adto);
                } else {
                    dto.setAddress(new AddressDto());
                }
            }
            model.addAttribute("accountDto", dto);
            return "admin/employee/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi mở form sửa: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/edit")
    public String handleEdit(@ModelAttribute("accountDto") AccountDto accountDto,
                             RedirectAttributes redirectAttributes) {
        try {
            accountService.update(accountDto);
            redirectAttributes.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

}
