package com.example.datn.repositories;

import com.example.datn.entities.BillDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillDetailRepository extends JpaRepository<BillDetails,Integer> {

    List<BillDetails> findByBillId(Integer billId);

}
