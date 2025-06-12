package com.example.datn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDto {

    private Integer id;

    private String code;

    private String fullName;

    private String email;

    private LocalDate birthOfDate;

    private String gender;

    private String roleName;

    private LocalDateTime created_at;

    private LocalDateTime updated_at;
}
