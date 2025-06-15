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
@Table(name = "material")
//CREATE TABLE material (
//        id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//        code NVARCHAR(50),
//        name NVARCHAR(100),
//        status BIT
//        );
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String name;

    private Boolean status;
}
