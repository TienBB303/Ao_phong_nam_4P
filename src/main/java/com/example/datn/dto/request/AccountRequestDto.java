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

    @Email(message = "Định dạng email không hợp lệ!")
    @NotBlank(message = "Email không được bỏ trống!")
    private String email;

    @NotBlank(message = "Mật khẩu không được bỏ trống!")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự!")
    private String password;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean status;
    private String avatarUsername; // Thêm nếu muốn cập nhật avatar
}
