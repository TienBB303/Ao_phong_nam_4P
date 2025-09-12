package com.example.datn.repositories;

import com.example.datn.dto.response.RevenueStatsDto;
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

    @Query("SELECT b FROM Bill b " +
            "WHERE (:code IS NULL OR b.code LIKE CONCAT('%', :code, '%')) " +
            "AND (:name IS NULL OR b.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:phone IS NULL OR b.phoneNumber LIKE CONCAT('%', :phone, '%')) " +
            "AND (:start IS NULL OR b.createdAt >= :start) " +
            "AND (:end IS NULL OR b.createdAt <= :end) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:typeBill IS NULL OR b.typeBill = :typeBill) " +
            "AND (b.status <> 9 AND b.status <> 10) " +
            "ORDER BY b.id DESC")
    Page<Bill> filterBills(
            @Param("code") String code,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Integer status,
            @Param("typeBill") Boolean typeBill,
            Pageable pageable
    );


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

//    @Query("SELECT b FROM Bill b " +
//            "WHERE (:code IS NULL OR b.code LIKE %:code%) " +
//            "AND (:name IS NULL OR b.name LIKE %:name%) " +
//            "AND (:phoneNumber IS NULL OR b.phoneNumber LIKE %:phoneNumber%) " +
//            "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
//            "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
//            "AND (:status IS NULL OR b.status = :status) " +
//            "AND (:typeBill IS NULL OR b.typeBill = :typeBill) " +
//            "ORDER BY b.id DESC")
//    Page<Bill> filterBills(
//            @Param("code") String code,
//            @Param("name") String name,
//            @Param("phoneNumber") String phoneNumber,
//            @Param("startDate") java.time.LocalDateTime startDate,
//            @Param("endDate") java.time.LocalDateTime endDate,
//            @Param("status") Integer status,
//            @Param("typeBill") Boolean typeBill,
//            Pageable pageable
//    );
    // ================== DASHBOARD ==================

    // Tổng tất cả đơn chờ xác nhận (status = 1, không phân biệt thanh toán)
    @Query(value = "SELECT COUNT(*) FROM bill WHERE status = 1 AND type_bill IN (0, 1)", nativeQuery = true)
    Long getTotalWaitingConfirmOrders();

    // Số đơn chờ xác nhận đã thanh toán (status = 1 AND payment_status = 1)
    @Query(value = "SELECT COUNT(*) FROM bill WHERE status = 1 AND payment_status = 1 AND type_bill IN (0, 1)", nativeQuery = true)
    Long getPaidWaitingConfirmOrders();

    //     Tổng tất cả đơn (mọi trạng thái, cả online + offline)
    //     => Dùng để hiển thị "Tổng đơn hàng"
    @Query(value = "SELECT COUNT(*) FROM bill WHERE status > 0 AND type_bill IN (0, 1)", nativeQuery = true)
    Long getTotalOrders();
    // Tổng đơn hàng (cả online + offline, chỉ tính hoàn thành)
    @Query(value = "SELECT COUNT(*) FROM bill WHERE status = 4 AND payment_status = 1 AND type_bill IN (0, 1)", nativeQuery = true)
    Long getTotalCompletedOrders();
    // Số đơn chờ xử lý (cả online + offline, Bao gồm: Chờ xác nhận (1), Đã xác nhận (2), Đang giao (3))
    @Query(value = "SELECT COUNT(*) FROM bill WHERE status IN (1, 2, 3) AND payment_status = 1 AND type_bill IN (0, 1)", nativeQuery = true)
    Long getPendingOrders();

    // Tổng doanh thu (cả online + offline, không tính phí ship)
    // - Đơn hoàn thành (status = 4): + doanh thu
// - Đơn giao thất bại (status = 5): - doanh thu (hoàn tiền)
// - Đơn online đã thanh toán qua app và được xác nhận: + doanh thu
    @Query(value = """
    SELECT COALESCE(SUM(
        CASE 
            WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            ELSE 0
        END
    ), 0)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
""", nativeQuery = true)
    BigDecimal getTotalRevenue();

    // Method lấy doanh thu và số đơn theo khoảng thời gian (cả online + offline)
    @Query(value = """
    SELECT COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   ELSE 0
               END
           ), 0),
           COUNT(*)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND b.created_at BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    Object[] getRevenueAndOrdersByRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Số đơn hàng hôm nay (cả online + offline)
    @Query(value = """
    SELECT COUNT(*) FROM bill
    WHERE status = 4 AND payment_status = 1 AND type_bill IN (0, 1)
      AND created_at BETWEEN :startOfDay AND :endOfDay
    """, nativeQuery = true)
    Long getTodayOrderCount(@Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);

    // Doanh thu 7 ngày gần nhất (cả online + offline)
    @Query(value = """
    SELECT CAST(b.created_at AS DATE) AS date,
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   ELSE 0
               END
           ), 0) AS totalRevenue
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND b.created_at BETWEEN :startDate AND :endDate
    GROUP BY CAST(b.created_at AS DATE)
    ORDER BY CAST(b.created_at AS DATE)
""", nativeQuery = true)
    List<Object[]> getRevenueLast7Days(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);


    // Đơn hàng theo trạng thái (cả online + offline)
    @Query(value = "SELECT b.status, COUNT(*) FROM bill b WHERE b.type_bill IN (0, 1) AND b.status IN (1,2,3,4,5,6) GROUP BY b.status",
            nativeQuery = true)
    List<Object[]> getOrderStatusCounts();

    // Tổng sản phẩm đã bán (chỉ tính đơn hoàn thành + thanh toán thành công)- (tính theo giá gốc ( dùng cho màn Danh sách sản phẩm bán nhiều nhất) (cả online + offline)
    @Query(value = """
        SELECT COALESCE(SUM(bd.quantity), 0)
        FROM bill_detail bd
        JOIN bill b ON b.id = bd.bill_id
        WHERE b.status = 4 AND b.payment_status = 1 AND b.type_bill IN (0, 1)
    """, nativeQuery = true)
    Long getTotalProductsSold();

    // Sản phẩm đã bán hôm nay (cả online + offline)
    @Query(value = """
        SELECT COALESCE(SUM(bd.quantity), 0)
        FROM bill_detail bd
        JOIN bill b ON b.id = bd.bill_id
        WHERE b.status = 4 AND b.payment_status = 1 AND b.type_bill IN (0, 1)
          AND b.created_at BETWEEN :startOfDay AND :endOfDay
    """, nativeQuery = true)
    Long getTodayProductsSold(@Param("startOfDay") LocalDateTime startOfDay,
                              @Param("endOfDay") LocalDateTime endOfDay);

    // ================== REVENUE (stats.html) ==================

    @Query(value = """
    SELECT COALESCE(SUM(
        CASE 
            WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            ELSE 0
        END
    ), 0)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND b.created_at BETWEEN :startOfDay AND :endOfDay
""", nativeQuery = true)
    BigDecimal getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay,
                               @Param("endOfDay") LocalDateTime endOfDay);

    // Doanh thu tháng hiện tại (cả online + offline)
    @Query(value = """
    SELECT COALESCE(SUM(
        CASE 
            WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            ELSE 0
        END
    ), 0)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND b.created_at BETWEEN :startOfMonth AND :endOfMonth
""", nativeQuery = true)
    BigDecimal getCurrentMonthRevenue(@Param("startOfMonth") LocalDateTime startOfMonth,
                                      @Param("endOfMonth") LocalDateTime endOfMonth);

    // Doanh thu năm hiện tại (cả online + offline)
    @Query(value = """
    SELECT COALESCE(SUM(
        CASE 
            WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
            ELSE 0
        END
    ), 0)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND YEAR(b.created_at) = YEAR(GETDATE())
""", nativeQuery = true)
    BigDecimal getCurrentYearRevenue();

    // Doanh thu theo khoảng ngày (cả online + offline)
    @Query(value = """
    SELECT CAST(b.created_at AS DATE),
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   ELSE 0
               END
           ), 0),
           COUNT(*)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND b.created_at BETWEEN :startDate AND :endDate
    GROUP BY CAST(b.created_at AS DATE)
    ORDER BY CAST(b.created_at AS DATE)
""", nativeQuery = true)
    List<Object[]> getRevenueByDate(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);


    // Doanh thu theo tháng trong năm (cả online + offline)
    @Query(value = """
    SELECT YEAR(b.created_at), MONTH(b.created_at),
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   ELSE 0
               END
           ), 0),
           COUNT(*)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
      AND YEAR(b.created_at) = :year
    GROUP BY YEAR(b.created_at), MONTH(b.created_at)
    ORDER BY YEAR(b.created_at), MONTH(b.created_at)
""", nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("year") int year);

    // Doanh thu theo năm (cả online + offline)
    @Query(value = """
    SELECT YEAR(b.created_at),
           COALESCE(SUM(
               CASE 
                   WHEN b.status = 4 THEN (b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   WHEN b.status = 5 THEN -(b.total_amount - COALESCE(b.shipping_fee,0) - COALESCE(b.discount_amount,0))
                   ELSE 0
               END
           ), 0),
           COUNT(*)
    FROM bill b
    WHERE b.type_bill IN (0, 1)
    GROUP BY YEAR(b.created_at)
    ORDER BY YEAR(b.created_at)
""", nativeQuery = true)
    List<Object[]> getRevenueByYear();

//    // Top sản phẩm bán chạy
//    @Query(value = """
//        SELECT p.name, SUM(bd.quantity), SUM(bd.total_price)
//        FROM bill_detail bd
//        JOIN bill b ON b.id = bd.bill_id
//        JOIN product_detail pd ON pd.id = bd.product_detail_id
//        JOIN product p ON p.id = pd.product_id
//        WHERE b.status = 4 AND b.payment_status = 1
//          AND b.created_at BETWEEN :startDate AND :endDate
//        GROUP BY p.name
//        ORDER BY SUM(bd.quantity) DESC
//    """, nativeQuery = true)
//    List<Object[]> getTopSellingProducts(@Param("startDate") LocalDateTime startDate,
//                                         @Param("endDate") LocalDateTime endDate);
    // TIENBB
    @Query("select c from Bill c where c.id = :id")
    Bill findByIdBill(Integer id);

    @Query("select cd from BillDetails cd where cd.bill.id = :id")
    List<BillDetails> findAllCartDetailByCartId(Integer id);

    @Query("select cd.bill from BillDetails cd where cd.id =:id")
    Bill findCartByCartDetailId(Integer id);

    @Query("select cd from Bill cd where cd.status = 9")
    List<Bill> getAllCartInline();
}