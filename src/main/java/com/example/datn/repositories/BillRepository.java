package com.example.datn.repositories;

import com.example.datn.entities.Bill;
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
    @Query("SELECT CAST(b.createdAt AS DATE) as date, " +
            "COALESCE(SUM(b.total_checkout), 0) as totalRevenue, " +
            "COUNT(b) as totalOrders " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startDate AND b.createdAt <= :endDate " +
            "GROUP BY CAST(b.createdAt AS DATE) " +
            "ORDER BY CAST(b.createdAt AS DATE)")
    List<Object[]> getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Thống kê doanh thu theo tháng
    @Query("SELECT YEAR(b.createdAt) as year, MONTH(b.createdAt) as month, " +
            "COALESCE(SUM(b.total_checkout), 0) as totalRevenue, " +
            "COUNT(b) as totalOrders " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND YEAR(b.createdAt) = :year " +
            "GROUP BY YEAR(b.createdAt), MONTH(b.createdAt) " +
            "ORDER BY YEAR(b.createdAt), MONTH(b.createdAt)")
    List<Object[]> getRevenueByMonth(@Param("year") int year);

    // Thống kê doanh thu theo năm
    @Query("SELECT YEAR(b.createdAt) as year, " +
            "COALESCE(SUM(b.total_checkout), 0) as totalRevenue, " +
            "COUNT(b) as totalOrders " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "GROUP BY YEAR(b.createdAt) " +
            "ORDER BY YEAR(b.createdAt)")
    List<Object[]> getRevenueByYear();

    // Tổng doanh thu và số đơn hàng theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(b.total_checkout), 0) as totalRevenue, " +
            "COUNT(b) as totalOrders " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Object[] getTotalRevenueAndOrdersByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // Doanh thu hôm nay
    @Query("SELECT COALESCE(SUM(b.total_checkout), 0) " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startOfDay AND b.createdAt <= :endOfDay")
    BigDecimal getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay,
                               @Param("endOfDay") LocalDateTime endOfDay);

    // Số đơn hàng hôm nay
    @Query("SELECT COUNT(b) " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startOfDay AND b.createdAt <= :endOfDay")
    Long getTodayOrderCount(@Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);

    // Top sản phẩm bán chạy
    @Query("SELECT pd.product.name, SUM(bd.quantity) as totalSold, " +
            "SUM(bd.total_price) as totalRevenue " +
            "FROM BillDetails bd " +
            "JOIN bd.bill b " +
            "JOIN bd.productDetail pd " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startDate AND b.createdAt <= :endDate " +
            "GROUP BY pd.product.id, pd.product.name " +
            "ORDER BY SUM(bd.quantity) DESC")
    List<Object[]> getTopSellingProducts(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(b.total_checkout) " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    BigDecimal getSimpleRevenueSum(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) " +
            "FROM Bill b " +
            "WHERE b.paymentStatus = true " +
            "AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Long getSimpleOrderCount(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);

    // Đếm số đơn theo status
    long countByStatus(int status);

    // Tổng doanh thu
    @Query("SELECT COALESCE(SUM(b.total_checkout), 0) FROM Bill b WHERE b.paymentStatus = true")
    java.math.BigDecimal getTotalRevenue();

    // Thống kê số lượng đơn hàng theo trạng thái
    @Query("SELECT b.status, COUNT(b) FROM Bill b GROUP BY b.status")
    java.util.List<Object[]> countOrdersByStatus();
}
