package com.example.datn.repositories;

import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Query("SELECT new com.example.datn.dto.response.AccountResponseDto(a.id, a.code, a.email, a.role.name, a.createdAt, a.updatedAt, a.status, a.avatarUsername) FROM Account a")
    List<AccountResponseDto> listAccountRes();

    Optional<Account> findTopByOrderByCodeDesc();

    boolean existsByEmail(String email);
    Optional<Account> findByEmail(String email);
}
