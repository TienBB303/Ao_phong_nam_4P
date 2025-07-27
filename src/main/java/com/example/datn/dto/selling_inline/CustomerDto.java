package com.example.datn.dto.selling_inline;

import com.example.datn.entities.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private Integer id;
    private String code;
    private String name;
    private String phoneNumber;
    private String email;
    private String displayName;

    public CustomerDto(Customer customer) {
        this.id = customer.getId();
        this.code = customer.getCode();
        this.name = customer.getName();
        this.phoneNumber = customer.getPhoneNumber();
        this.email = customer.getAccount().getEmail();
        this.displayName = customer.getName() + " (" + customer.getPhoneNumber() + ")";
    }

}
