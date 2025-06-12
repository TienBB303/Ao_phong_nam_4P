package com.example.datn.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequestDto {

    private String fullName;

    private String email;

    private String password;

    private String phoneNumber;

    private LocalDate birthOfDate;

    private String gender;
}
