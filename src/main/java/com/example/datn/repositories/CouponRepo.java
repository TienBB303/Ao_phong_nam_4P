package com.example.datn.repositories;

import com.example.datn.entities.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepo extends JpaRepository<Coupon,Integer> {
}
