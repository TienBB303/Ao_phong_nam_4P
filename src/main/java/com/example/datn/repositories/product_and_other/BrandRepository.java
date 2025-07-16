package com.example.datn.repositories.product_and_other;

import com.example.datn.entities.product_and_other.Brand;
import com.example.datn.entities.product_and_other.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    @Query("SELECT abc FROM Brand abc")
    List<Brand> getAll();

    @Query("SELECT abc FROM Brand abc ORDER BY abc.id DESC")
    Page<Brand> getAll(Pageable pageable);

    @Query("SELECT abc FROM Brand abc WHERE abc.id = :id")
    Brand findByIdBrand(Integer id);

    @Query("SELECT abc FROM Brand abc WHERE LOWER(abc.name) = LOWER(:name)")
    Brand findByName(String name);

    @Query("SELECT abc FROM Brand abc WHERE abc.code = :code")
    Brand findByCode(String code);

    @Query("SELECT abc FROM Brand abc " +
            "WHERE ((LOWER(abc.code) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(abc.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:status IS NULL OR abc.status = :status)) " +
            "ORDER BY abc.id DESC")
    Page<Brand> search(String query, Boolean status, Pageable pageable);

    @Query("SELECT MAX(b.code) FROM Brand b WHERE b.code LIKE 'BR%'")
    String findMaxCodeBrand();

    //Khanh
    @Query("SELECT b FROM Brand b WHERE b.status = true")
    List<Brand> findAllActive();

}
