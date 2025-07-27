package com.example.datn.repositories;

import com.example.datn.entities.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod,Integer> {
    @Query("select p from PaymentMethod p where lower(p.name) like lower(concat('%', :name , '%')) ")
    PaymentMethod findPaymentMethodByName(String name);


}
