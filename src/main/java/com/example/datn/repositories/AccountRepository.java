package com.example.datn.repositories;

import com.example.datn.dto.response.AccountResponseDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Selling.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Query("SELECT new com.example.datn.dto.response.AccountResponseDto(a.id, a.code, a.email, a.role.name, a.createdAt, a.updatedAt, a.status, a.avatarUsername) FROM Account a")
    List<AccountResponseDto> listAccountRes();

    Optional<Account> findTopByOrderByCodeDesc();

    boolean existsByEmail(String email);
    Optional<Account> findByEmail(String email);
    // Lấy danh sách account phân trang, loại trừ theo role name
    Page<Account> findByRole_NameNot(String roleName, Pageable pageable);
    // Lấy danh sách account theo đúng role name
    Page<Account> findByRole_Name(String roleName, Pageable pageable);
    @Query("select c from Cart c where c.account.id = :accountId")
    Cart findByAccountID(Integer accountId);

    Page<Account> findByCodeStartingWith(String prefix, Pageable pageable);
}
