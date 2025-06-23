package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT c FROM Category c")
    List<Category> getAll();

    @Query("SELECT c FROM Category c ORDER BY c.id DESC")
    Page<Category> getAll(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Category findByIdCategory(Integer id);

    @Query("SELECT c FROM Category c WHERE c.name = :name")
    Category findByName(String name);

    @Query("SELECT c FROM Category c " +
            "WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND (:status IS NULL OR c.status = :status)) " +
            "ORDER BY c.id DESC")
    Page<Category> search(String name, Boolean status, Pageable pageable);
}
