package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT c FROM Category c")
    List<Category> getAll();

    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Category findByIdCategory(Integer id);
}
