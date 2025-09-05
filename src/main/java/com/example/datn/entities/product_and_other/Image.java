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
@Table(name = "image")
//CREATE TABLE image (
    //id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    //name NVARCHAR(100),
    //path_file NVARCHAR(200),
    //product_detail_id INT FOREIGN KEY REFERENCES product_detail(id)			-- 17/07/2025 : thêm product detail ở đây
//);
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String path_file;

    @ManyToOne
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;
}
