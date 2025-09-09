package com.example.datn.services;


import com.example.datn.entities.Account;
import com.example.datn.entities.Bill;
import com.example.datn.entities.BillHistory;
import com.example.datn.repositories.AccountRepository;
import com.example.datn.repositories.BillHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class BillHistoryService {
    @Autowired
    private BillHistoryRepository billHistoryRepository;
    @Autowired
    private AccountRepository accountRepository;


    public void saveHistory(Bill bill, int status, String note) {
        BillHistory history = new BillHistory();
        history.setBill(bill);
        history.setStatus(status);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Account) {
            Account account = (Account) auth.getPrincipal();
            history.setAccount(account);
        }


        billHistoryRepository.save(history);
    }
}
