
package com.example.datn.controllers;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.repositories.BillRepository;
import com.example.datn.services.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("admin/bill")
public class BillController {
     @Autowired
    BillService billService;
    @GetMapping("/view")
    public String index( @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size,
            Model model) {
        Page<Bill> billPage = billService.getAllBills(PageRequest.of(page, size));
        model.addAttribute("bills", billPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", billPage.getTotalPages());
        return "admin/bill";
    }

    @GetMapping("/update-bill-status/{billId}")
    public String updateBillStatus(Model model,
                                   @PathVariable Integer billId,
                                   @RequestParam("trangThaiDonHang") String trangThaiDonHang,
                                   RedirectAttributes redirectAttributes) {
        try {
            int status;
            try {
                status = Integer.parseInt(trangThaiDonHang);
                if (status < 1 || status > 5) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error",
                        "Trạng thái đơn hàng không hợp lệ: " + trangThaiDonHang);
                return "redirect:/admin/getbill-detail/" + billId;
            }

            Bill bill = billService.updateStatus(trangThaiDonHang, billId);
            redirectAttributes.addFlashAttribute("message",
                    "Hóa đơn " + bill.getCode() + " cập nhật trạng thái thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật trạng thái.");
        }

        return "redirect:/admin/getbill-detail/" + billId;
    }
    @GetMapping("/getbill-detail/{maHoaDon}")
    public String getBillDetail(Model model, @PathVariable("maHoaDon") Integer maHoaDon) {
        // Lấy hóa đơn theo ID
        Bill bill = billService.findById(maHoaDon);
        if (bill == null) {
            model.addAttribute("error", "Không tìm thấy hóa đơn có mã: " + maHoaDon);
            return "redirect:/admin/bill/view";
        }

        // Lấy danh sách chi tiết hóa đơn
        List<BillDetails> billDetailsList = billService.findBillDetailsByBillId(maHoaDon);

        // Tính tổng tiền
        double total = billDetailsList.stream()
                .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
                .sum();

        // Đưa vào model
        model.addAttribute("bill", bill);
        model.addAttribute("billDetails", billDetailsList);
        model.addAttribute("total", total);
        return "admin/billDetail";
    }

}

