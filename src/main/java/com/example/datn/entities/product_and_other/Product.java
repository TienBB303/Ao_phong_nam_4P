package com.example.datn.entities.product_and_other;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
//CREATE TABLE product (
//        id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//        code NVARCHAR(50),
//        name NVARCHAR(100),
//        status BIT,
//        description NVARCHAR(200),
//        category_id INT FOREIGN KEY REFERENCES category(id),
//        brand_id INT FOREIGN KEY REFERENCES brand(id),
//        material_id INT FOREIGN KEY REFERENCES material(id)
//        );

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String name;

    private Boolean status;

    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;
}
