package com.example.datn.controllers.admin;

import com.example.datn.repositories.BillRepository;
import com.example.datn.repositories.product_and_other.ProductRepository;
import com.example.datn.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping({"/admin", "/admin/dashboard"})
public class DashboardController {
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String dashboardGUI(Model model) {
        // ✅ 1. TỔNG ĐƠN ĐẶT HÀNG (sử dụng method có sẵn)
        Long totalOrders = billRepository.getTotalCompletedOrders();

        // ✅ 2. SỐ ĐƠN CHỜ XỬ LÝ (sử dụng method có sẵn)
        Long pendingOrders = billRepository.getPendingOrders();

        // ✅ 3. TỔNG DOANH THU (sử dụng method có sẵn - không tính phí ship)
        BigDecimal totalRevenue = billRepository.getTotalRevenue();

        // ✅ 4. SỐ SẢN PHẨM ĐÃ BÁN (sử dụng method có sẵn)
        Long totalProducts = billRepository.getTotalProductsSold();

        // ✅ 5. ĐƠN HÀNG THEO TRẠNG THÁI (sử dụng method có sẵn)
        List<Object[]> statusCounts = billRepository.getOrderStatusCounts();
        Map<String, Long> orderStatusMap = new LinkedHashMap<>();

        for (Object[] row : statusCounts) {
            Integer status = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            String label = getStatusLabel(status);
            orderStatusMap.put(label, count);
        }

        // ✅ 6. DOANH THU 7 NGÀY GẦN NHẤT (sử dụng method có sẵn)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        List<Object[]> revenueData = billRepository.getRevenueLast7Days(startDate, endDate);

        List<String> dateLabels = new ArrayList<>();
        List<BigDecimal> revenueValues = new ArrayList<>();

        for (Object[] row : revenueData) {
            // row[0] = date, row[1] = revenue
            String date = row[0].toString();
            BigDecimal revenue = (row[1] != null) ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;

            dateLabels.add(formatDateForChart(date));
            revenueValues.add(revenue);
        }

        // ✅ THÊM CÁC ATTRIBUTE VÀO MODEL
        model.addAttribute("totalOrders", totalOrders != null ? totalOrders : 0L);
        model.addAttribute("pendingOrders", pendingOrders != null ? pendingOrders : 0L);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("totalProducts", totalProducts != null ? totalProducts : 0L);
        model.addAttribute("orderStatusMap", orderStatusMap);
        model.addAttribute("dateLabels", dateLabels);
        model.addAttribute("revenueValues", revenueValues);

        // Debug log
        System.out.println("Dashboard Data:");
        System.out.println("- Total Orders: " + totalOrders);
        System.out.println("- Pending Orders: " + pendingOrders);
        System.out.println("- Total Revenue: " + totalRevenue);
        System.out.println("- Total Products Sold: " + totalProducts);
        System.out.println("- Revenue Data Size: " + revenueData.size());

        return "admin/dashboard";
    }

    /**
     * Convert status number to readable label
     */
    private String getStatusLabel(Integer status) {
        switch (status) {
            case 1: return "Chờ xác nhận";
            case 2: return "Đã xác nhận";
            case 3: return "Đang giao";
            case 4: return "Hoàn thành";
            case 5: return "Đã hủy";
            case 9: return "Tại quầy";
            default: return "Không rõ";
        }
    }

    /**
     * Format date for chart display (DD/MM/YYYY)
     */
    private String formatDateForChart(String dateStr) {
        try {
            // Nếu dateStr là định dạng YYYY-MM-DD
            if (dateStr.contains("-")) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    return parts[2] + "/" + parts[1] + "/" + parts[0];
                }
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }

}