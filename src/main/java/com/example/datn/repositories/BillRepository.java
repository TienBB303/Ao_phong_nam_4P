package com.example.datn.repositories;

import com.example.datn.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill,Integer> {
    @Query("SELECT MAX(b.code) FROM Bill b WHERE b.code LIKE 'HD%' ")
    String findMaxCodeBill();

    @Query("select b from Bill b where b.id = :id")
    Bill findBillById(Integer id);
}
