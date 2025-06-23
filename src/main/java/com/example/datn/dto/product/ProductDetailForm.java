package com.example.datn.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailForm {
    private Integer colorId;
    private Integer sizeId;
    private BigDecimal price;
    private Integer quantity;
}
