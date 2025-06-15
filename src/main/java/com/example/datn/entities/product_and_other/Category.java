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
@Table(name = "category")
//CREATE TABLE category (
//        id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//        name NVARCHAR(100),
//        description NVARCHAR(200),
//        status BIT
//        );
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String description;

    private Boolean status;
}
