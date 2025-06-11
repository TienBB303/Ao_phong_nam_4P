package com.example.datn.Repository;

import com.example.datn.Entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepo extends JpaRepository<Coupon,Integer> {
}
