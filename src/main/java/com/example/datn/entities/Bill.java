package com.example.datn.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "bill")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    private String code;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "payment_status")
    private Integer paymentStatus;

    private Integer status;

    @Column(name = "delivery_type")
    private Integer deliveryType;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "payment_method")
    private Integer paymentMethod;

    @Column(name = "discount_id")
    private Long discountId;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

}
