package com.example.datn.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "bill")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    private String code;

    private String address_shipping;

    private String note;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "payment_status")
    private Boolean paymentStatus;

    private Integer status; // 9 là tại quầy

    @Column(name = "type_bill")
    private Boolean typeBill;


    private Boolean delivery_type;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;
    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    @OneToMany(mappedBy = "bill", fetch = FetchType.LAZY)
    private List<BillDetails> billDetails;


    private BigDecimal total_checkout; // thành tiền

    @Transient
    private Integer total_quantity;    // tổng số lượng


    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                '}';
    }
}
