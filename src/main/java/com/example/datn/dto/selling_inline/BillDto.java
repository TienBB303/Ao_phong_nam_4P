package com.example.datn.dto.selling_inline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

//    CREATE TABLE bill (
//            id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
//    code NVARCHAR(50),
//    discount_amount DECIMAL(10, 2),
//    total_amount DECIMAL(10, 2),
//    payment_status NVARCHAR(50),
//    status NVARCHAR(50),
//    delivery_type NVARCHAR(50),
//    shipping_fee DECIMAL(10, 2),
//    name NVARCHAR(100),
//    phone_number NVARCHAR(20),
//    email NVARCHAR(100),
//    payment_method_id INT FOREIGN KEY REFERENCES payment_method(id),
//    discount_id INT FOREIGN KEY REFERENCES discount(id),
//    customer_id INT FOREIGN KEY REFERENCES customer(id),
//);
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillDto {

    private String code;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private Integer paymentStatus;

    private Integer status;

    private  Integer deliveryType;

    private BigDecimal shippingFee;

    private String name;

    private String phoneNumber;

    private String email;

    private Integer paymentMethodId;

    private Integer customerId;

    private Long discountId;
}
