package com.example.datn.dto.request;

import jakarta.validation.constraints.*;
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
public class AccountRequestDto {

    private Integer id;

    private String code;
    @NotBlank(message = "Ho ten khong duoc bo trong!")
    private String fullName;

    @Email(message = "Dinh dang email khong hop le!")
    @NotBlank(message = "Email khong duoc bo trong!")
    private String email;

    @NotBlank(message = "Mat khau khong duoc bo trong!")
    @Size(min = 6, message = "Mat khau toi thieu 8 ky tu!")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "SDT chua dung dinh dang!")
    private String phoneNumber;

    @Past(message = "Ngay sinh phai o qua khu!")
    private LocalDate birthOfDate;

    @Pattern(regexp = "^(Nam|Nữ)$", message = "Gioi tinh phai la Nam hoac Nu!")
    private String gender;

    private LocalDateTime created_at;

    private LocalDateTime updated_at;
}
