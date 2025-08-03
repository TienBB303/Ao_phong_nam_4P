package com.example.datn.repositories;


import com.example.datn.entities.BillHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillHistoryRepository extends JpaRepository<BillHistory,Integer> {
    List<BillHistory> findByBillIdOrderByCreatedAtDesc(Integer billId);

}
