package com.example.datn.repositories;

import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Query("SELECT new com.example.datn.dto.response.AccountResponseDto(a.id, a.code, a.fullName, a.email, a.birthOfDate, a.gender, a.role.name, a.created_at, a.updated_at) FROM Account a")
    List<AccountResponseDto> listAccountRes();


    @Query("SELECT new com.example.datn.dto.response.AccountResponseDto(a.id, a.code, a.fullName, a.email, a.birthOfDate, a.gender, a.role.name, a.created_at, a.updated_at) FROM Account a")
    Page<AccountResponseDto> paginate(Pageable pageable);

    @Query("SELECT new com.example.datn.dto.response.AccountResponseDto(a.id, a.code, a.fullName, a.email, a.birthOfDate, a.gender, a.role.name, a.created_at, a.updated_at) FROM Account a WHERE a.code =:code")
    List<AccountResponseDto> detailByCode(String code);

    Optional<Account> findTopByOrderByCodeDesc();
}
