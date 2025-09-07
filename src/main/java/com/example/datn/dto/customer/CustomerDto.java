package com.example.datn.dto.customer;

import com.example.datn.dto.AddressDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private Integer id;
    private String code;
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 50)
    private String name;
    private Boolean gender; // true = nam, false = nữ
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate birthday;
    @NotBlank(message = "SĐT không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.")
    private String phoneNumber;
    @Email(message = "Email không đúng định dạng!")
    private String email;
    private Boolean isActive;
    // Địa chỉ được thêm nhanh khi tạo mới
    private AddressDto address;

    // getter/setter hoặc @Data của Lombok
}