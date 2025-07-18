package com.example.datn.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code", unique = true, nullable = false) // code thường là duy nhất và không null
    private String code;
    @Nationalized
    @Column(name = "name", nullable = false)
    private String name;
    private Boolean gender; // true = nam, false = nữ
    @Column(name = "phone_number", unique = true) // Số điện thoại thường là duy nhất
    private String phoneNumber;
    @Column(name = "birth_of_date")
    private LocalDate birthDate;
    @Column(name = "is_active")
    private Boolean isActive = true;
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private Account account;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingAddress> addresses;

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}