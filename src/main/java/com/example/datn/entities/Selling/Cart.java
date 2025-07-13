package com.example.datn.entities.Selling;

import com.example.datn.entities.Account;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//CREATE TABLE cart (
    //id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    //total_price_cart DECIMAL(10, 2),
    //total_quantity INT,
    //created_at DATETIME DEFAULT GETDATE(),
    //updated_at DATETIME DEFAULT GETDATE(),
    //status NVARCHAR(50),
    //account_id INT FOREIGN KEY REFERENCES account(id),
//);
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal total_price_cart;
    private Integer total_quantity;
    private Date created_at;
    private Date updated_at;
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}
