package com.example.datn.entities;

import com.example.datn.entities.product_and_other.ProductDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

//CREATE TABLE bill_detail (
    //id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    //price DECIMAL(10, 2),
    //quantity INT,
    //total_price DECIMAL(10, 2),
    //bill_id INT FOREIGN KEY REFERENCES bill(id),
    //product_detail_id INT FOREIGN KEY REFERENCES product_detail(id)
//);
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bill_detail")
public class BillDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal total_price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;

}
