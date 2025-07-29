package com.example.datn.repositories;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillDetailRepository extends JpaRepository<BillDetails,Integer> {

    List<BillDetails> findByBillId(Integer billId);
    List<BillDetails> findByBill(Bill bill);

    // TIEnBB
    @Query("select cd from BillDetails cd where cd.id = :id")
    BillDetails findCartDetailById(Integer id);

    @Query("select cd from BillDetails  cd where cd.bill.id = :cartId And cd.productDetail.id = :productDetailId")
    BillDetails findByCartAndProductDetailId(Integer cartId, Integer productDetailId);

    @Query("select count(cd) from BillDetails cd where cd.bill.id = :id")
    Integer countItemInCartByCartId(Integer id);

    @Query("select sum(cd.quantity) from BillDetails cd where cd.bill.id = :id")
    Integer countAllItemInCartByCartId(Integer id);
}
