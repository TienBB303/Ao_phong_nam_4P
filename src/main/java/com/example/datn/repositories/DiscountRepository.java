package com.example.datn.repositories;

import com.example.datn.entities.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount,Integer> {

    Page<Discount> findAllByOrderByIdDesc(Pageable pageable);


    @Query("SELECT d FROM Discount d " +
            "WHERE (:code IS NULL OR d.code LIKE CONCAT('%', :code, '%')) " +
            "AND (:codeName IS NULL OR d.codeName LIKE CONCAT('%', :codeName, '%')) " +
            "AND (:start IS NULL OR d.startDatetime >= :start) " +
            "AND (:end IS NULL OR d.endDatetime <= :end) " +
            "AND (:type IS NULL OR d.discountType = :type) " +
            "AND (:status IS NULL OR d.status = :status) " +
            "ORDER BY d.id DESC")
    Page<Discount> filterDiscounts(
            @Param("code") String code,
            @Param("codeName") String codeName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("type") String type,
            @Param("status") Integer status,
            Pageable pageable
    );
    boolean existsByCodeName(String codeName);
    @Query("SELECT COUNT(d) > 0 FROM Discount d WHERE d.codeName = :name AND d.id <> :id")
    boolean existsByCodeNameAndNotId(@Param("name") String name, @Param("id") int id);

    boolean existsByCode(String code);
    @Query("SELECT MAX(CAST(SUBSTRING(d.code, 3) AS int)) FROM Discount d WHERE d.code LIKE 'DC%'")
    Integer findMaxCodeNumber();
    Optional<Discount> findByCode(String code);

    //TienBB
    @Query("select d from Discount d where d.minPurchase <= :minPrice")
    List<Discount> getAllDiscountByMinPurchase(BigDecimal minPrice);

    //TienBB
    @Query("select c.discount from Cart c where c.id = :id")
    Discount findDiscountByCartId(Integer id);
}
