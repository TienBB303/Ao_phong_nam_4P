package com.example.datn.repositories;

import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.entities.ShippingAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Integer> {
    //    boolean existsByEmail(String email);
    //check unique
    boolean existsByCode(String code);

    boolean existsByPhoneNumber(String phoneNumber);

//    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND (c.name LIKE %:keyword% OR c.code LIKE %:keyword%)")
    @Query("SELECT c FROM Customer c WHERE (c.name LIKE %:keyword% OR c.code LIKE %:keyword%)")
    Page<Customer> searchCustomerKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Customer> findByIsActiveTrue(Pageable pageable);
    // Chỉ lấy Customer có account.role.name = :roleName
    @Query("SELECT c FROM Customer c JOIN c.account a JOIN a.role r WHERE r.name = :roleName")
    Page<Customer> findCustomersByRoleName(@Param("roleName") String roleName, Pageable pageable);

    // Tìm kiếm theo keyword nhưng vẫn lọc đúng role
    @Query("SELECT c FROM Customer c JOIN c.account a JOIN a.role r WHERE r.name = :roleName AND (c.name LIKE %:keyword% OR c.code LIKE %:keyword%)")
    Page<Customer> searchCustomersByRoleName(@Param("roleName") String roleName, @Param("keyword") String keyword, Pageable pageable);

    // ma tu sinh
    Customer findTopByOrderByIdDesc();

    long countByIsActiveTrue();
    // Đếm số khách hàng theo role name (ROLE_CUSTOMER)
    @Query("SELECT COUNT(c) FROM Customer c JOIN c.account a JOIN a.role r WHERE r.name = :roleName")
    long countCustomersByRoleName(@Param("roleName") String roleName);

    @Query("select c from Customer c where " +
            "lower(c.name) like lower(concat('%', :keyword, '%') ) " +
            "or lower(c.phoneNumber) like lower(concat('%', :keyword, '%') ) ")
    List<Customer> searchCustomerByKeywordInline(@Param("keyword") String keyword);


    @Query("select c from Customer c where lower(c.name) like lower(concat('%%', :name, '%')) " +
            "or c.phoneNumber like :phoneNumber ")
    Customer searchCustomerExistNameOrPhoneInline(String name, String phoneNumber);

    // ✅ THÊM MỚI: Tìm Customer với addresses được load sẵn (khắc phục LazyInitializationException)
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
    Optional<Customer> findByIdWithAddresses(@Param("id") Integer id);

    // ✅ THÊM MỚI: Tìm Customer với cả addresses và account được load sẵn
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN FETCH c.addresses " +
            "LEFT JOIN FETCH c.account " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithAddressesAndAccount(@Param("id") Integer id);

    // ✅ THÊM MỚI: Tìm Customer theo account email với addresses
    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN FETCH c.addresses " +
            "JOIN FETCH c.account a " +
            "WHERE a.email = :email")
    Optional<Customer> findByAccountEmailWithAddresses(@Param("email") String email);


//    @Query("select c from Customer c where lower(c.name) like lower(concat('%%', :name, '%')) " +
//            "or c.phoneNumber like :phoneNumber ")
//    Customer searchCustomerExistNameOrPhoneInline(String name, String phoneNumber);

    @Query("select c from Customer c where c.phoneNumber like :phoneNumber ")
    Customer searchCustomerExistPhoneInline(String phoneNumber);


    //TienBB thêm query truy vấn tìm tất cả địa chỉ của khách
    @Query("select sa from ShippingAddress sa where sa.customer.id = :id")
    List<ShippingAddress> findAllShippingAddressOfCustomer(Integer id);
    //TienBB thêm query truy vấn tìm account by user
    @Query("SELECT a FROM Account a WHERE a.customer.id = :customerId")
    Account findAccountByCustomerID(Integer customerId);

    Customer findByAccount(Account account);
}
