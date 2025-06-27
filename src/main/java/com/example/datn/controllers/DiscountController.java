package com.example.datn.controllers;

import com.example.datn.entities.Discount;
import com.example.datn.repositories.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Controller
@RequestMapping("/admin/discount")
public class DiscountController {
    @Autowired
    DiscountRepository discountRepo;


    @GetMapping("/view")
    public String viewDiscounts(
            @RequestParam(defaultValue = "") String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        // 1. Chuyển đổi ngày
        LocalDateTime startDateTime = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = (end != null) ? end.atTime(23, 59, 59) : null;

        // 2. Phân trang
        Pageable pageable = PageRequest.of(page, size);

        // 3. Lọc dữ liệu từ DB (không filter thủ công nữa)
        Page<Discount> pageDiscounts = discountRepo.filterDiscounts(
                code.isEmpty() ? null : code,
                startDateTime,
                endDateTime,
                type.isEmpty() ? null : type,
                status,
                pageable
        );
        for (Discount d : pageDiscounts.getContent()) {
            LocalDateTime now = LocalDateTime.now();
            int newStatus;

            if (d.getEndDatetime().isBefore(now)) {
                newStatus = 3; // Đã kết thúc
            } else if (d.getStartDatetime().isAfter(now)) {
                newStatus = 2; // Sắp diễn ra
            } else {
                newStatus = (d.getStatus() != 4) ? 1 : 4; // Đang diễn ra hoặc vẫn giữ là Đã đóng
            }

            if (d.getStatus() == null || d.getStatus() != newStatus) {
                d.setStatus(newStatus);
                discountRepo.save(d); // Cập nhật khi cần
            }
        }


        // 4. Đẩy dữ liệu ra view
        model.addAttribute("list", pageDiscounts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageDiscounts.getTotalPages());

        // 5. Truyền lại dữ liệu lọc để giữ lại input
        model.addAttribute("code", code);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("type", type);
        model.addAttribute("status", status);

        return "admin/discountList";
    }


    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("discount", new Discount());
        return "admin/discountCreate";
    }


    @PostMapping("/create")
    public String create(@ModelAttribute("discount") Discount discount, RedirectAttributes redirectAttributes) {
        LocalDateTime now = LocalDateTime.now();

        // Set status dựa trên ngày bắt đầu/kết thúc
        if (discount.getEndDatetime().isBefore(now)) {
            discount.setStatus(3); // Đã kết thúc
        } else if (discount.getStartDatetime().isAfter(now)) {
            discount.setStatus(2); // Sắp diễn ra
        } else {
            discount.setStatus(1); // Đang diễn ra
        }
        redirectAttributes.addFlashAttribute("success", "Thêm mã giảm giá thành công!");
        discountRepo.save(discount);
        return "redirect:/admin/discount/view";
    }


    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        Discount discount = discountRepo.findById(id).orElse(null);
        model.addAttribute("discount", discount);
        return "admin/discountEdit";
    }


    @PostMapping("/update/{id}")
    public String update(@PathVariable int id, @ModelAttribute("discount") Discount discount,RedirectAttributes redirectAttributes) {
        discount.setId(id);

        LocalDateTime now = LocalDateTime.now();
        if (discount.getEndDatetime().isBefore(now)) {
            discount.setStatus(3);
        } else if (discount.getStartDatetime().isAfter(now)) {
            discount.setStatus(2);
        } else {
            discount.setStatus(1);
        }
        redirectAttributes.addFlashAttribute("success", "Cập nhật mã giảm giá thành công!");
        discountRepo.save(discount);
        return "redirect:/admin/discount/view";
    }



    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model) {
        Discount discount = discountRepo.findById(id).orElse(null);
        model.addAttribute("discount", discount);
        return "admin/discountDetail";
    }
    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Discount discount = discountRepo.findById(id).orElse(null);
        if (discount != null) {
            LocalDateTime now = LocalDateTime.now();

            // Nếu đã kết thúc thì không cho thay đổi trạng thái
            if (discount.getEndDatetime() != null && discount.getEndDatetime().isBefore(now)) {
                redirectAttributes.addFlashAttribute("error", "Mã giảm giá đã kết thúc, không thể thay đổi trạng thái.");
                return "redirect:/admin/discount/view";
            }

            if (discount.getStatus() != null && discount.getStatus() == 4) {
                discount.setStatus(1); // Mở lại
            } else {
                discount.setStatus(4); // Đóng
            }
            discountRepo.save(discount);
        }
        return "redirect:/admin/discount/view";
    }

}
