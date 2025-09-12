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
        Account account = accountRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("❌ Email không tồn tại: " + email));

        // Mapping số role -> tên role
        String roleName;
        switch (account.getRole().getId()) { // giả sử role có id = 1,2,3
            case 1 -> roleName = "ROLE_CUSTOMER";
            case 2 -> roleName = "ROLE_EMPLOYEE";
            case 3 -> roleName = "ROLE_ADMIN";
            default-> roleName = "ROLE_CUSTOMER";
        }

        // ✅ Log ra để debug
        System.out.println("🔑 Email login: " + account.getEmail());
        System.out.println("🔑 DB RoleId: " + account.getRole().getId());
        System.out.println("🔑 Mapped RoleName: " + roleName);
        System.out.println("🔑 Password in DB: " + account.getPassword());

        return new User(
                account.getEmail(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }
}
