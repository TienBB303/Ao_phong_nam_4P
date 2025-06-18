package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MaterialCategory extends JpaRepository<Material, Integer> {
    @Query("SELECT m FROM Material m")
    List<Material> getAll();

    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Material findByIdMaterial(Integer id);
}
