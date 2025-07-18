package com.example.datn.dto.employee;
import com.example.datn.dto.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Integer id;

    private String code;

    private String fullName;

    private String email;

    private String phoneNumber;

    private LocalDate birthDate;

    private Boolean gender;

    private String password;

    private Boolean status;

    private String roleName;

    private AddressDto address;
}
