package com.example.datn.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    private String fullName;

    private String email;

    private String password;

    private String phoneNumber;

    private LocalDate birthOfDate;

    private String gender;

    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
