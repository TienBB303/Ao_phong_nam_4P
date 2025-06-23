package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Image;
import com.example.datn.entities.product_and_other.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    @Query("SELECT i FROM Image i")
    List<Image> getAll();

    @Query("SELECT i FROM Image i ORDER BY i.id DESC")
    Page<Image> getAll(Pageable pageable);

    @Query("SELECT i FROM Image i WHERE i.id = :id")
    Image findByIdImage(Integer id);
}
