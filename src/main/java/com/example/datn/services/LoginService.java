package com.example.datn.services;

import com.example.datn.entities.Account;
import com.example.datn.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Đang kiểm tra email: " + email);

        Account account = accountRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("❌ Email không tồn tại: " + email));

        String roleName = "ROLE_" + account.getRole().getName().toUpperCase();

        return new User(
                account.getEmail(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }

}
