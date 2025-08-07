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
    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping
    public String dashboardGUI(Model model) {
        // Tổng đơn hàng
        long totalOrders = billRepository.countByStatusAndPaid(4,1); // Đếm hóa đơn hoàn thành + đã thanh toán // Status = 4 (hoàn thành), Paid = 1 (đã thanh toán)
        long pendingOrders = billRepository.countByStatusAndPaid(1,1); // Đếm đơn chờ xử lý + đã thanh toán // Status = 1 (chờ xử lý), Paid = 1 (đã thanh toán)
        // Tổng doanh thu
        LocalDateTime fromDate = LocalDateTime.of(2020, 1, 1, 0, 0); // Hoặc ngày đầu năm 2020
        LocalDateTime toDate = LocalDateTime.now(); // Tổng doanh thu hóa đơn hoàn thành

        System.out.println("Revenue từ " + fromDate + " đến " + toDate);
        BigDecimal totalRevenue = billRepository.getSimpleRevenueSumWithStatus(fromDate, toDate, 4);
        // Tổng sản phẩm
        long totalProducts = productRepository.countTotalProducts();

        // Đơn hàng theo trạng thái cho biểu đồ tròn
        List<Object[]> statusCounts = billRepository.countOrdersByStatus();
        Map<String, Long> orderStatusMap = new LinkedHashMap<>();
        for (Object[] row : statusCounts) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            String label;
            switch (status) {
                case 1: label = "Chờ xác nhận"; break;
                case 2: label = "Đã xác nhận"; break;
                case 3: label = "Đang giao"; break;
                case 4: label = "Hoàn thành"; break;
                case 5: label = "Đã hủy"; break;
                default: label = "Không rõ"; break;
            }
            orderStatusMap.put(label, count);
        }

        // Tổng số khách hàng
        long customerCount = customerRepository.count();
        model.addAttribute("customerCount", customerCount);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("orderStatusMap", orderStatusMap);
        return "admin/dashboard";
    }
}