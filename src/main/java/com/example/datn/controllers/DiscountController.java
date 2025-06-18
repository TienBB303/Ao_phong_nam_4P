package com.example.datn.controllers;

import com.example.datn.entities.Discount;
import com.example.datn.repositories.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/admin/discount")
public class DiscountController {
    @Autowired
    DiscountRepository discountRepo;

    // Hiển thị danh sách Discount (phân trang)
    @GetMapping("/view")
    public String viewDiscounts(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Discount> pageDiscounts = discountRepo.findAll(pageable);

        model.addAttribute("list", pageDiscounts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageDiscounts.getTotalPages());
        return "admin/discountList";
    }
    // Hiển thị form tạo mới
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("discount", new Discount());
        return "admin/discountCreate";
    }

    // Xử lý tạo mới
    @PostMapping("/create")
    public String create(@ModelAttribute("discount") Discount discount) {
        discountRepo.save(discount);
        return "redirect:/admin/discount/view";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        Discount discount = discountRepo.findById(id).orElse(null);
        model.addAttribute("discount", discount);
        return "admin/discountEdit";
    }

    // Xử lý cập nhật
    @PostMapping("/update/{id}")
    public String update(@PathVariable int id, @ModelAttribute("discount") Discount discount) {
        discount.setId(id);
        discountRepo.save(discount);
        return "redirect:/admin/discount/view";
    }

    // Xem chi tiết
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model) {
        Discount discount = discountRepo.findById(id).orElse(null);
        model.addAttribute("discount", discount);
        return "admin/discountDetail";
    }
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable int id) {
        Discount discount = discountRepo.findById(id).orElse(null);
        if (discount != null) {
            discount.setStatus(!discount.isStatus());
            discountRepo.save(discount);
        }
        return "redirect:/admin/discount/view";
    }
}
