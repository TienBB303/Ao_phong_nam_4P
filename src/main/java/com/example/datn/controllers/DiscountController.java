package com.example.datn.controllers;

import com.example.datn.entities.Discount;
import com.example.datn.services.DiscountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;




@Controller
@RequestMapping("/admin/discount")
public class DiscountController {
    @Autowired
    private DiscountService discountService;

    @GetMapping("/view")
    public String viewDiscounts(
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "") String codeName,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        Page<Discount> pageDiscounts = discountService.getFilteredDiscounts(code,codeName, start, end, type, status, page, size);

        model.addAttribute("list", pageDiscounts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageDiscounts.getTotalPages());
        model.addAttribute("code", code);
        model.addAttribute("codeName", codeName);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("type", type);
        model.addAttribute("status", status);

        return "admin/discountList";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("discount")) {
            model.addAttribute("discount", new Discount());
        }
        return "admin/discountCreate";
    }
    @PostMapping("/create")
    public String create(@ModelAttribute("discount") @Valid Discount discount,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/discountCreate";
        }
        try {
            discountService.saveDiscount(discount);
            redirectAttributes.addFlashAttribute("success", "Thêm mã giảm giá thành công!");
            return "redirect:/admin/discount/view";
        } catch (RuntimeException e) {
            model.addAttribute("discount", discount);
            model.addAttribute("error", e.getMessage());
            return "admin/discountCreate";
        }
    }
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        discountService.getDiscountById(id).ifPresent(d -> model.addAttribute("discount", d));
        return "admin/discountEdit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable int id,
                         @Valid @ModelAttribute("discount") Discount discount,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/discountEdit";
        }

        discount.setId(id);
        try {
            discountService.saveDiscount(discount);
            redirectAttributes.addFlashAttribute("success", "Cập nhật mã giảm giá thành công!");
            return "redirect:/admin/discount/view";
        } catch (RuntimeException e) {
            model.addAttribute("discount", discount);
            model.addAttribute("error", e.getMessage());
            return "admin/discountEdit";
        }
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model) {
        discountService.getDiscountById(id).ifPresent(d -> model.addAttribute("discount", d));
        return "admin/discountDetail";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable int id, RedirectAttributes redirectAttributes) {
        String errorMsg = discountService.toggleStatus(id);
        if (errorMsg != null) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
        }
        return "redirect:/admin/discount/view";
    }
}
