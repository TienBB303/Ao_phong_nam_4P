package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    @Query("SELECT b FROM Brand b")
    List<Brand> getAll();

    @Query("SELECT b FROM Brand b WHERE b.id = :id")
    Brand findByIdBrand(Integer id);
}
