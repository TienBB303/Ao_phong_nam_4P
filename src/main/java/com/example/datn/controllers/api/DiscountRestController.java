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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/discount")
public class DiscountRestController {

    @Autowired
    private DiscountRepository discountRepository;

    @GetMapping("/apply")
    public ResponseEntity<?> applyDiscount(@RequestParam String code) {
        Optional<Discount> optionalDiscount = discountRepository.findByCode(code.trim());

        if (optionalDiscount.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Mã giảm giá không tồn tại."));
        }

        Discount discount = optionalDiscount.get();

        if (discount.getStartDatetime() == null || discount.getEndDatetime() == null ||
                discount.getStartDatetime().isAfter(LocalDateTime.now()) ||
                discount.getEndDatetime().isBefore(LocalDateTime.now())) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Mã giảm giá đã hết hạn hoặc chưa đến thời gian áp dụng."));
        }


        Map<String, Object> result = new HashMap<>();
        result.put("discountAmount", discount.getDiscountValue());
        result.put("message", "Áp dụng mã giảm giá thành công");

        return ResponseEntity.ok(result);
    }
}