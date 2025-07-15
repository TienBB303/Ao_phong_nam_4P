package com.example.datn.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Nationalized;

@Data
@Entity
@Table(name = "shipping_address")
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Nationalized
    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name")
    @Nationalized
    private String provinceName;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district_name")
    @Nationalized
    private String districtName;

    @Column(name = "ward_id")
    private String wardId;

    @Column(name = "ward_name")
    @Nationalized
    private String wardName;

    @Column(name = "receiver_name")
    @Nationalized
    private String receiverName;

    @Column(name = "receiver_phone_number")
    private String receiverPhoneNumber;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
