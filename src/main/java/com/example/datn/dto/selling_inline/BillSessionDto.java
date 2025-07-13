package com.example.datn.dto.selling_inline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BillSessionDto {
    private BillDto billDto;
    private List<BillDetailDto> billDetailDtos = new ArrayList<>();
}
