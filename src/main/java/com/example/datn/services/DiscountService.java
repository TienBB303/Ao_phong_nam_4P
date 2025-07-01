package com.example.datn.services;

import com.example.datn.entities.Discount;
import com.example.datn.repositories.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DiscountService {
    @Autowired
    private DiscountRepository discountRepository;

    public Page<Discount> getFilteredDiscounts(String code, LocalDate start, LocalDate end, String type, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime startDateTime = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime endDateTime = (end != null) ? end.atTime(23, 59, 59) : null;

        Page<Discount> pageDiscounts = discountRepository.filterDiscounts(
                code.isEmpty() ? null : code,
                startDateTime,
                endDateTime,
                type.isEmpty() ? null : type,
                status,
                pageable
        );

        LocalDateTime now = LocalDateTime.now();
        for (Discount d : pageDiscounts.getContent()) {
            int newStatus = 1;

            if (d.getEndDatetime() != null && d.getEndDatetime().isBefore(now)) {
                newStatus = 3;
            } else if (d.getStartDatetime() != null && d.getStartDatetime().isAfter(now)) {
                newStatus = 2;
            } else if (d.getStatus() != null && d.getStatus() == 4) {
                newStatus = 4;
            }

            if (d.getStatus() == null || d.getStatus() != newStatus) {
                d.setStatus(newStatus);
                discountRepository.save(d);
            }
        }

        return pageDiscounts;
    }

    public Discount saveDiscount(Discount discount) {
        String inputCode = discount.getCode() != null ? discount.getCode().trim() : null;
        Integer id = discount.getId(); // null nếu thêm mới

        if (inputCode == null || inputCode.isEmpty()) {
            // Tự động sinh mã
            Integer nextNumber = discountRepository.findMaxCodeNumber();
            if (nextNumber == null) nextNumber = 0;

            String newCode;
            do {
                nextNumber++;
                newCode = String.format("DC%02d", nextNumber);
            } while (discountRepository.existsByCode(newCode));

            discount.setCode(newCode);
        } else {
            // Nếu người dùng nhập code thủ công
            Optional<Discount> existing = discountRepository.findByCode(inputCode);

            // Nếu đang thêm mới hoặc đang sửa nhưng code đã bị thay đổi
            if ((id == null && existing.isPresent()) ||
                    (id != null && existing.isPresent() && !existing.get().getId().equals(id))) {
                throw new RuntimeException("Mã giảm giá đã tồn tại");
            }

            discount.setCode(inputCode);
        }

        // Check trùng tên cũng chỉ nếu đang thêm mới hoặc sửa nhưng tên đổi
        String inputName = discount.getCodeName().trim();
        boolean nameExists = discountRepository.existsByCodeName(inputName);
        if ((id == null && nameExists) ||
                (id != null && nameExists && !discountRepository.findById(id).get().getCodeName().equalsIgnoreCase(inputName))) {
            throw new RuntimeException("Tên mã giảm giá đã tồn tại");
        }
        discount.setCodeName(inputName);

        LocalDateTime now = LocalDateTime.now();
        if (discount.getEndDatetime() != null && discount.getEndDatetime().isBefore(now)) {
            discount.setStatus(3);
        } else if (discount.getStartDatetime() != null && discount.getStartDatetime().isAfter(now)) {
            discount.setStatus(2);
        } else {
            discount.setStatus(1);
        }
        return discountRepository.save(discount);
    }



    public Optional<Discount> getDiscountById(int id) {
        return discountRepository.findById(id);
    }

    public String toggleStatus(int id) {
        Discount discount = discountRepository.findById(id).orElse(null);
        if (discount == null) {
            return "Không tìm thấy mã giảm giá.";
        }

        Integer status = discount.getStatus();

        if (status == 3) { // Đã kết thúc
            return "Mã giảm giá đã kết thúc, không thể thay đổi trạng thái.";
        }

        if (status == 2) { // Sắp diễn ra
            return "Mã giảm giá chưa hoạt động, không thể thay đổi trạng thái.";
        }

        // Đang diễn ra (1) → chuyển sang Đã đóng (4), hoặc ngược lại
        if (status != null && status == 4) {
            discount.setStatus(1); // Mở lại
        } else {
            discount.setStatus(4); // Đóng
        }

        discountRepository.save(discount);
        return "Cập nhật trạng thái thành công!";
    }

}
