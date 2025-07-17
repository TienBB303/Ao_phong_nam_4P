package com.example.datn.entities;

import jakarta.persistence.*;
import lombok.*;

//-- Bảng payment_method
//CREATE TABLE payment_method (
//id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//name NVARCHAR(100)
//);
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_method")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
}
