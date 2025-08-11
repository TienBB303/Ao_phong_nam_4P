package com.example.datn.controllers.api;

import com.example.datn.entities.Discount;
import com.example.datn.repositories.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/discount")
public class DiscountRestController {

    @Autowired
    private DiscountRepository discountRepository;

    // ✅ Áp dụng mã giảm giá cho khách vãng lai (nhập tay)
    @GetMapping("/apply")
    public ResponseEntity<?> applyDiscount(@RequestParam String code) {
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Vui lòng nhập mã giảm giá."));
        }

        Optional<Discount> optionalDiscount = discountRepository.findByCode(code.trim());

        if (optionalDiscount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Mã giảm giá không tồn tại."));
        }

        Discount discount = optionalDiscount.get();

        if (!isDiscountValid(discount)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Mã giảm giá không hợp lệ hoặc đã hết hạn."));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("discountId", discount.getId());
        result.put("discountAmount", discount.getDiscountValue());
        result.put("message", "Áp dụng mã giảm giá thành công");

        return ResponseEntity.ok(result);
    }

    // ✅ Lấy danh sách mã giảm giá hợp lệ dành cho người đã đăng nhập
    @GetMapping("/valid")
    public ResponseEntity<?> getValidDiscountsForLoggedInUser() {
        List<Discount> validDiscounts = discountRepository.findAll().stream()
                .filter(this::isDiscountValid)
                .collect(Collectors.toList());

        return ResponseEntity.ok(validDiscounts);
    }

    // ✅ Hàm kiểm tra hợp lệ
    private boolean isDiscountValid(Discount discount) {
        LocalDateTime now = LocalDateTime.now();

        return discount.getStatus() != null &&
                discount.getStatus() == 1 &&
                discount.getStartDatetime() != null &&
                discount.getEndDatetime() != null &&
                !discount.getStartDatetime().isAfter(now) &&
                !discount.getEndDatetime().isBefore(now) &&
                discount.getUsageLimit() != null &&
                discount.getUsageLimit() > 0;
    }
}


