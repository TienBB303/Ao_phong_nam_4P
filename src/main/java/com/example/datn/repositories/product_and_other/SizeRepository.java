package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Size;
import com.example.datn.entities.product_and_other.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SizeRepository extends JpaRepository<Size, Integer> {
    @Query("SELECT s FROM Size s")
    List<Size> getAll();

    @Query("SELECT s FROM Size s ORDER BY s.id DESC")
    Page<Size> getAll(Pageable pageable);

    @Query("SELECT s FROM Size s WHERE s.id  = :id")
    Size findByIdSize(Integer id);

    @Query("SELECT s FROM Size s WHERE LOWER(s.name) = LOWER(:name)")
    Size findByName(String name);

    @Query("SELECT s FROM Size s WHERE LOWER(s.code) = LOWER(:code)")
    Size findByCode(String code);

    @Query("SELECT s FROM Size s " +
            "WHERE ((LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:status IS NULL OR s.status = :status)) " +
            "ORDER BY s.id DESC")
    Page<Size> search(String query, Boolean status, Pageable pageable);
}
