package com.example.datn.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductForm {
    private String code;
    private String name;
    private Integer categoryId;
    private Integer brandId;
    private Integer materialId;
    private String description;

    private List<ProductDetailForm> variants;

    private List<Integer> selectedColorIds;
    private List<Integer> selectedSizeIds;
}
