package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import com.example.datn.entities.product_and_other.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Integer> {
    @Query("SELECT m FROM Material m")
    List<Material> getAll();

    @Query("SELECT m FROM Material m ORDER BY m.id DESC")
    Page<Material> getAll(Pageable pageable);

    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Material findByIdMaterial(Integer id);

    @Query("SELECT m FROM Material m WHERE LOWER(m.name) = LOWER(:name)")
    Material findByName(String name);

    @Query("SELECT m FROM Material m WHERE LOWER(m.code) = LOWER(:code)")
    Material findByCode(String code);

    @Query("SELECT m FROM Material m " +
            "WHERE ((LOWER(m.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:status IS NULL OR m.status = :status)) " +
            "ORDER BY m.id DESC")
    Page<Material> search(String query, Boolean status, Pageable pageable);

    @Query("SELECT MAX(m.code) FROM Material m WHERE m.code LIKE 'MA%'")
    String findMaxCodeMaterial();

    //Khanh
    @Query("SELECT b FROM Material b WHERE b.status = true")
    List<Material> findAllActive();

}
