package com.example.datn.repositories;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.Selling.Cart;
import com.example.datn.entities.Selling.CartDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill,Integer> {
    @Query("SELECT MAX(b.code) FROM Bill b WHERE b.code LIKE 'HD%' ")
    String findMaxCodeBill();

    @Query("select b from Bill b where b.id = :id")
    Bill findBillById(Integer id);
    Page<Bill> findAll(Pageable pageable);
    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.discount WHERE b.id = :id")
    Bill findWithDiscountById(Integer id);

    @Query("SELECT b.code FROM Bill b WHERE b.code LIKE 'HD___' ORDER BY b.code DESC")
    List<String> findOfflineBillCodes();

    @Query("SELECT b FROM Bill b WHERE b.code = :code")
    Bill findByCode(@Param("code") String code);

    @Query("SELECT b FROM Bill b " +
            "LEFT JOIN FETCH b.billDetails bd " +
            "LEFT JOIN FETCH bd.productDetail pd " +
            "LEFT JOIN FETCH pd.product p " +
            "LEFT JOIN FETCH pd.color c " +
            "LEFT JOIN FETCH pd.size s " +
            "WHERE b.code = :code AND b.typeBill = :targetTypeBill")
    Bill findByCodeWithAllDetailsAndTypeBill(@Param("code") String code, @Param("targetTypeBill") Boolean targetTypeBill);

    @Query("SELECT b FROM Bill b " +
            "WHERE (:code IS NULL OR b.code LIKE %:code%) " +
            "AND (:name IS NULL OR b.name LIKE %:name%) " +
            "AND (:phoneNumber IS NULL OR b.phoneNumber LIKE %:phoneNumber%) " +
            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:typeBill IS NULL OR b.typeBill = :typeBill) " +
            "ORDER BY b.id DESC")
    Page<Bill> filterBills(
            @Param("code") String code,
            @Param("name") String name,
            @Param("phoneNumber") String phoneNumber,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("status") Integer status,
            @Param("typeBill") Boolean typeBill,
            Pageable pageable
    );
    // Thống kê doanh thu theo ngày
    @Query(value = """
    SELECT CAST(b.created_at AS DATE) AS date, 
           COALESCE(SUM(b.total_checkout), 0) AS totalRevenue, 
           COUNT(*) AS totalOrders 
    FROM bill b 
    WHERE b.payment_status = 1 AND b.status = 4 
      AND b.created_at BETWEEN :startDate AND :endDate 
    GROUP BY CAST(b.created_at AS DATE) 
    ORDER BY CAST(b.created_at AS DATE)
    """, nativeQuery = true)
    List<Object[]> getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // TIENBB
    @Query("select c from Bill c where c.id = :id")
    Bill findByIdBill(Integer id);

    @Query("select cd from BillDetails cd where cd.bill.id = :id")
    List<BillDetails> findAllCartDetailByCartId(Integer id);

    @Query("select cd.bill from BillDetails cd where cd.id =:id")
    Bill findCartByCartDetailId(Integer id);

    @Query("select cd from Bill cd where cd.status = 9")
    List<Bill> getAllCartInline();
    // Thống kê doanh thu theo tháng
    @Query(value = """
    SELECT COALESCE(SUM(total_checkout), 0) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = :status 
      AND created_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    BigDecimal getSimpleRevenueSumWithStatus(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("status") int status);
    // 4. Doanh thu tháng hiện tại
    @Query(value = """
    SELECT COALESCE(SUM(total_checkout), 0) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND created_at BETWEEN :startOfMonth AND :endOfMonth
""", nativeQuery = true)
    BigDecimal getCurrentMonthRevenue(@Param("startOfMonth") LocalDateTime startOfMonth,
                                      @Param("endOfMonth") LocalDateTime endOfMonth);


    @Query(value = """
    SELECT COUNT(*) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = :status 
      AND created_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Long getSimpleOrderCountWithStatus(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("status") int status);

    @Query(value = """
    SELECT YEAR(created_at), MONTH(created_at), 
           COALESCE(SUM(total_checkout), 0), COUNT(*) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND YEAR(created_at) = :year 
    GROUP BY YEAR(created_at), MONTH(created_at) 
    ORDER BY YEAR(created_at), MONTH(created_at)
""", nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("year") int year);

    // Thống kê doanh thu theo năm
    @Query(value = """
    SELECT YEAR(created_at), 
           COALESCE(SUM(total_checkout), 0), 
           COUNT(*) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
    GROUP BY YEAR(created_at) 
    ORDER BY YEAR(created_at)
""", nativeQuery = true)
    List<Object[]> getRevenueByYear();

    // Tổng doanh thu và số đơn hàng theo khoảng thời gian
    @Query(value = """
    SELECT COALESCE(SUM(total_checkout), 0), COUNT(*) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND created_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Object[] getTotalRevenueAndOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // Doanh thu hôm nay
    @Query(value = """
    SELECT COALESCE(SUM(total_checkout), 0) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND created_at BETWEEN :startOfDay AND :endOfDay
""", nativeQuery = true)
    BigDecimal getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay,
                               @Param("endOfDay") LocalDateTime endOfDay);

    // Số đơn hàng hôm nay
    @Query(value = """
    SELECT COUNT(*) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND created_at BETWEEN :startOfDay AND :endOfDay
""", nativeQuery = true)
    Long getTodayOrderCount(@Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);

    // Top sản phẩm bán chạy
    @Query(value = """
    SELECT p.name, SUM(bd.quantity), SUM(bd.total_price)
    FROM bill_detail bd
    JOIN bill b ON b.id = bd.bill_id
    JOIN product_detail pd ON pd.id = bd.product_detail_id
    JOIN product p ON p.id = pd.product_id
    WHERE b.payment_status = 1 AND b.status = 4
      AND b.created_at BETWEEN :startDate AND :endDate
    GROUP BY p.name
    ORDER BY SUM(bd.quantity) DESC
""", nativeQuery = true)
    List<Object[]> getTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);


    @Query(value = """
    SELECT COALESCE(SUM(total_checkout), 0) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND created_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    BigDecimal getSimpleRevenueSum(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT COUNT(*) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4 
      AND created_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Long getSimpleOrderCount(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);


    // Đếm số đơn theo status
    @Query(value = "SELECT COUNT(*) FROM bill WHERE status = :status AND payment_status = :paid", nativeQuery = true)
    Long countByStatusAndPaid(@Param("status") int status, @Param("paid") int paid);

//    // Tổng doanh thu
//    @Query("SELECT COALESCE(SUM(b.total_checkout), 0) FROM Bill b WHERE b.paymentStatus = true")
//    java.math.BigDecimal getTotalRevenue();

    // Thống kê số lượng đơn hàng theo trạng thái
    @Query("SELECT b.status, COUNT(b) FROM Bill b GROUP BY b.status")
    java.util.List<Object[]> countOrdersByStatus();

    // Tổng doanh thu hóa đơn hoàn thành
    @Query(value = """
    SELECT COALESCE(SUM(total_checkout), 0) 
    FROM bill 
    WHERE payment_status = 1 
      AND status = 4
""", nativeQuery = true)
    BigDecimal getTotalRevenueCompleted();
}