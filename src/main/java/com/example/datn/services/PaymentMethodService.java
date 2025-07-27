package com.example.datn.services;

import com.example.datn.entities.PaymentMethod;
import com.example.datn.repositories.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMethodService {
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public PaymentMethod findByPaymentMethodName(String name){
        return paymentMethodRepository.findPaymentMethodByName(name);
    }

    public List<PaymentMethod> getAllPaymentMethods(){
        return paymentMethodRepository.findAll();
    }
}
