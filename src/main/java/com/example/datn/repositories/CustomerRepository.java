package com.example.datn.repositories;

import com.example.datn.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Integer> {
//    boolean existsByEmail(String email);
    //check unique
    boolean existsByCode(String code);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND (c.name LIKE %:keyword% OR c.code LIKE %:keyword%)")
    Page<Customer> searchCustomerKeyword(@Param("keyword") String keyword, Pageable pageable);
    Page<Customer> findByIsActiveTrue(Pageable pageable);
    // ma tu sinh
    Customer findTopByOrderByIdDesc();

    long countByIsActiveTrue();


    @Query("select c from Customer c where " +
            "lower(c.name) like lower(concat('%', :keyword, '%') ) " +
            "or lower(c.phoneNumber) like lower(concat('%', :keyword, '%') ) ")
    List<Customer> searchCustomerByKeywordInline(@Param("keyword") String keyword);

}
