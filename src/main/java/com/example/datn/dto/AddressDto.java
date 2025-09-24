package com.example.datn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String addressDetail;

    // Bỏ validate bắt buộc cho 2 trường này
    private String receiverPhoneNumber;
    private String receiverName;

    @NotNull(message = "Tỉnh/Thành phố không được để trống")
    private Integer provinceId;

    private String provinceName;

    @NotNull(message = "Quận/Huyện không được để trống")
    private Integer districtId;

    private String districtName;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String wardId;

    private String wardName;

    // isDefault = true nếu nhân viên tick chọn địa chỉ là mặc định. quan que gi z
    private Boolean isDefault = false;
}
