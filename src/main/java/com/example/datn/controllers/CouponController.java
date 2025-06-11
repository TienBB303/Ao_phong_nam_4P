package com.example.datn.controllers;

import com.example.datn.entities.Coupon;
import com.example.datn.repositories.CouponRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CouponController {
    @Autowired
    CouponRepo counponRepo;

    @GetMapping("/discount/view")
    public List<Coupon> View() {
        return counponRepo.findAll();
    }

    @GetMapping("/discount/PhanTrang")
    public List<Coupon> phanTrang(@RequestParam(defaultValue = "0") int p) {
        Pageable pageable = PageRequest.of(p, 1);
        Page<Coupon> page = counponRepo.findAll(pageable);
        return page.getContent();
    }

    @PutMapping("/discount/update/{id}")
    public Coupon Update(@PathVariable int id, @RequestBody Coupon discountEntity) {
        discountEntity.setId(id);
        counponRepo.save(discountEntity);
        return discountEntity;
    }

    @GetMapping("/discount/Detail/{id}")
    public Coupon Detail(@PathVariable int id) {
        return counponRepo.findById(id).orElse(null);
    }

    @PostMapping("/discount/create")
    public Coupon add(@RequestBody Coupon discountEntity) {
        counponRepo.save(discountEntity);
        return discountEntity;
    }
}
