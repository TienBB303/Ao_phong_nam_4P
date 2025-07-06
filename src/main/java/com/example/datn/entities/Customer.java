package com.example.datn.entities;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Nationalized;

@Data
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
    @Column(name = "phone_number", unique = true) // Số điện thoại thường là duy nhất
    private String phoneNumber;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "is_active")
    private Boolean isActive = true;
    @Nationalized
    @Column(name = "address") 
    private String address;

//    @OneToOne(mappedBy = "customer")
//    private Account account;

}
