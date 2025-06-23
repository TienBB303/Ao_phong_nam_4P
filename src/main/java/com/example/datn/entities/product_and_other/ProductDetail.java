package com.example.datn.entities.product_and_other;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_detail")
public class ProductDetail {
//    CREATE TABLE product_detail (
//            id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//    price DECIMAL(10, 2),
//    quantity INT,
//    barcode NVARCHAR(100),
//    product_id INT FOREIGN KEY REFERENCES product(id),
//    color_id INT FOREIGN KEY REFERENCES color(id),
//    size_id INT FOREIGN KEY REFERENCES size(id),
//    image_id INT FOREIGN KEY REFERENCES image(id)
//);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal price;

    private Integer quantity;

    private String barcode;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;
}
