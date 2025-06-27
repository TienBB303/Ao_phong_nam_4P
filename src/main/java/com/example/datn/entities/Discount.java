package com.example.datn.entities;

import jakarta.persistence.*;
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

    @Column(name = "code_name")
    private String codeName;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDatetime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDatetime;

    @Column(name = "min_purchase")
    private BigDecimal minPurchase;

    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer status;

//

    @Transient
    public String getDisplayStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (status != null && status == 4) {
            return "Đã đóng";
        }
        if (endDatetime != null && endDatetime.isBefore(now)) {
            return "Đã kết thúc";
        } else if (startDatetime != null && startDatetime.isAfter(now)) {
            return "Sắp diễn ra";
        } else {
            return "Đang diễn ra";
        }
    }
}
