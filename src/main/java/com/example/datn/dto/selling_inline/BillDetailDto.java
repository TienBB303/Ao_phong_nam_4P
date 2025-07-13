package com.example.datn.dto.selling_inline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//CREATE TABLE bill_detail (
//        id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//quantity INT,
//bill_id INT FOREIGN KEY REFERENCES bill(id),
//product_detail_id INT FOREIGN KEY REFERENCES product_detail(id)
//);
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillDetailDto {
    private Long id;

    private Integer quantity;

    private Integer billId;

    private Integer productDetailId;
}
