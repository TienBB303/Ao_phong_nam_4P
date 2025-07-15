package com.example.datn.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "discount")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @NotBlank(message = "Tên mã giảm giá không được để trống")
    @Column(name = "code_name")
    private String codeName;

    @NotBlank(message = "Phương thức giảm không được để trống")
    @Column(name = "discount_type")
    private String discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDatetime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDatetime;

    @NotNull(message = "Giá trị đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Không được âm")
    @Column(name = "min_purchase")
    private BigDecimal minPurchase;
    @NotNull(message = "Giá trị tối đa không được để trống")
    @Min(value = 0, message = "Không được âm")
    @Column(name = "max_discount")
    private BigDecimal maxDiscount;
    @NotNull(message = "Số lượng sử dụng tối đa không được để trống")
    @Min(value = 0, message = "Không được âm")

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer status;



    @Transient
    public String getDisplayStatus() {
        switch (this.status) {
            case 1:
                return "Đang diễn ra";
            case 2:
                return "Sắp diễn ra";
            case 3:
                return "Đã kết thúc";
            case 4:
                return "Đã đóng";
            default:
                return "Không xác định";
        }
    }
//    @AssertTrue(message = "Ngày bắt đầu phải nhỏ hơn ngày kết thúc")
//    @Transient
//    public boolean isStartBeforeEnd() {
//        if (startDatetime == null || endDatetime == null) {
//            return true;
//        }
//        return startDatetime.isBefore(endDatetime);
//    }
//
//    @AssertTrue(message = "Ngày kết thúc phải sau thời điểm hiện tại")
//    @Transient
//    public boolean isEndAfterNow() {
//        if (endDatetime == null) {
//            return true;
//        }
//        return endDatetime.isAfter(LocalDateTime.now());
//    }

}
