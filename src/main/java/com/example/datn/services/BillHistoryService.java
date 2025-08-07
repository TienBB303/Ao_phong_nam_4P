package com.example.datn.services;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillHistory;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.BillHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BillHistoryService {
    @Autowired
    private BillHistoryRepository billHistoryRepository;

    @Autowired
    private BillService billService;

    public void saveHistory(Bill bill, int status, String note) {
        BillHistory history = new BillHistory();
        history.setBill(bill);
        history.setStatus(status);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());

        billHistoryRepository.save(history);
    }
    public void updateStatus(Integer billId, Integer status, String note) {
        Bill bill = billService.findById(billId);
        bill.setStatus(status);
        billService.save(bill);

        BillHistory history = new BillHistory();
        history.setBill(bill);
        history.setStatus(status);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        billHistoryRepository.save(history);
    }
}
