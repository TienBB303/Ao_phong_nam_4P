package com.example.datn.repositories;
import com.example.datn.entities.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Integer>{
    // ✅ Cập nhật tất cả các địa chỉ của khách hàng về không mặc định
    @Modifying
    @Transactional
    @Query("UPDATE ShippingAddress sa SET sa.isDefault = false WHERE sa.customer.id = :customerId")
    void updateAllDefaultFalseByCustomerId(Integer customerId);

    // ✅ Lấy tất cả địa chỉ của 1 khách hàng (nếu cần dùng hiển thị danh sách)
    List<ShippingAddress> findByCustomerId(Integer customerId);

    // ✅ Lấy địa chỉ mặc định của 1 khách hàng (nếu cần)
    ShippingAddress findByCustomerIdAndIsDefaultTrue(Integer customerId);

    @Modifying
    @Query("UPDATE ShippingAddress a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void clearDefault(Integer customerId);
}
