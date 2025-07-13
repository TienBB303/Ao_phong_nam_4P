package com.example.datn.dto.selling_inline;

import com.example.datn.entities.product_and_other.ProductDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String displayName;
    private String barCode;
    private Integer quantity;

    public ProductDetailDto(ProductDetail pd) {
        this.id = pd.getId();
        this.name = pd.getProduct().getName();
        this.price = pd.getPrice();
        this.displayName = pd.getProduct().getName() + " (" + pd.getSize().getCode() + " - " + pd.getColor().getName() + ")";
        this.barCode = pd.getBarcode();
        this.quantity = pd.getQuantity();
    }
}
