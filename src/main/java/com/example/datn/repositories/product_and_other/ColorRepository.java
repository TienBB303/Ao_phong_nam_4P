package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.entities.product_and_other.Color;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ColorRepository extends JpaRepository<Color, Integer> {
    @Query("SELECT c FROM Color c")
    List<Color> getAll();

    @Query("SELECT c FROM Color c ORDER BY c.id DESC")
    Page<Color> getAll(Pageable pageable);

    @Query("SELECT c FROM Color c WHERE c.id = :id")
    Color findByIdColor(Integer id);

    @Query("SELECT c FROM Color c WHERE c.name = :name")
    Color findByName(String name);

    @Query("SELECT c FROM Color c WHERE c.code = :code")
    Color findByCode(String code);

    @Query("SELECT c FROM Color c " +
            "WHERE ((LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:status IS NULL OR c.status = :status)) " +
            "ORDER BY c.id DESC")
    Page<Color> search(String query, Boolean status, Pageable pageable);
}
