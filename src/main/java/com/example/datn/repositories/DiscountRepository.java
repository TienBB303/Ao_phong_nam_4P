package com.example.datn.repositories;

import com.example.datn.entities.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount,Integer> {

    Page<Discount> findAllByOrderByIdDesc(Pageable pageable);


    @Query("SELECT d FROM Discount d " +
            "WHERE (:code IS NULL OR d.code LIKE CONCAT('%', :code, '%')) " +
            "AND (:start IS NULL OR d.startDatetime >= :start) " +
            "AND (:end IS NULL OR d.endDatetime <= :end) " +
            "AND (:type IS NULL OR d.discountType = :type) " +
            "AND (:status IS NULL OR d.status = :status) " +
            "ORDER BY d.id DESC")
    Page<Discount> filterDiscounts(
            @Param("code") String code,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("type") String type,
            @Param("status") Integer status,
            Pageable pageable
    );
}
