package com.example.datn.repositories;

import com.example.datn.entities.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BillRepository extends JpaRepository<Bill,Integer> {
    @Query("SELECT MAX(b.code) FROM Bill b WHERE b.code LIKE 'HD%' ")
    String findMaxCodeBill();

    @Query("select b from Bill b where b.id = :id")
    Bill findBillById(Integer id);
    Page<Bill> findAll(Pageable pageable);
    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.discount WHERE b.id = :id")
    Bill findWithDiscountById(Integer id);

    @Query("SELECT b FROM Bill b " +
            "WHERE (:code IS NULL OR b.code LIKE %:code%) " +
            "AND (:name IS NULL OR b.name LIKE %:name%) " +
            "AND (:phoneNumber IS NULL OR b.phoneNumber LIKE %:phoneNumber%) " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:typeBill IS NULL OR b.typeBill = :typeBill) " +
            "ORDER BY b.id DESC")
    Page<Bill> filterBills(
            @Param("code") String code,
            @Param("name") String name,
            @Param("phoneNumber") String phoneNumber,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("status") Integer status,
            @Param("typeBill") Boolean typeBill,
            Pageable pageable
    );

}
