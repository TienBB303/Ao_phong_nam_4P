package com.example.datn.dto.bill;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BillInsert {

    private String fullName;

    private String phone;

    private String email;

    private String province;

    private String district;

    private String ward;

    private String street;

    private String note;

    private String discountCode;

    private BigDecimal discountValue;

    private Integer discountId;

}
