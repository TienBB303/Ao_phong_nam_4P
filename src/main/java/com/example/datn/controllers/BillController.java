
package com.example.datn.controllers;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.entities.BillHistory;
import com.example.datn.repositories.BillHistoryRepository;
import com.example.datn.repositories.BillRepository;
import com.example.datn.services.BillHistoryService;
import com.example.datn.services.BillService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("admin/bill")
public class BillController {
     @Autowired
    BillService billService;
     @Autowired
    BillHistoryService billHistoryService;
     @Autowired
    BillHistoryRepository billHistoryRepository;
    @GetMapping("/view")
    public String index(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean typeBill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {

        Page<Bill> billPage = billService.getFilteredBills(code, name, phoneNumber, start, end, status, typeBill, page,size);

        model.addAttribute("bills", billPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", billPage.getTotalPages());

        // giữ lại điều kiện lọc
        model.addAttribute("code", code);
        model.addAttribute("name", name);
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("status", status);
        model.addAttribute("type", typeBill);

        return "admin/bill";
    }


    @GetMapping("/update-bill-status/{billId}")
    public String updateBillStatus(Model model,
                                   @PathVariable Integer billId,
                                   @RequestParam("trangThaiDonHang") Integer trangThaiDonHang,
                                   @RequestParam(value = "note", required = false) String note,
                                   RedirectAttributes redirectAttributes) {
        try {
//            int status = Integer.parseInt(trangThaiDonHang);

            if (trangThaiDonHang < 1 || trangThaiDonHang > 6) {
                redirectAttributes.addFlashAttribute("error", "Trạng thái đơn hàng không hợp lệ.");
                return "redirect:/admin/bill/getbill-detail/" + billId;
            }

            Bill bill = billService.updateStatus(trangThaiDonHang, billId);


            billHistoryService.saveHistory(bill, trangThaiDonHang, note);

            redirectAttributes.addFlashAttribute("message",
                    "Hóa đơn " + bill.getCode() + " cập nhật trạng thái thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật trạng thái.");
        }

        return "redirect:/admin/bill/getbill-detail/" + billId;
    }

    @GetMapping("/getbill-detail/{maHoaDon}")
    public String getBillDetail(Model model, @PathVariable("maHoaDon") Integer maHoaDon) {
        // Lấy hóa đơn theo ID
        Bill bill = billService.findByIdWithDiscount(maHoaDon);
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

        System.out.println("Bill status class = " + bill.getStatus().getClass().getName());
        // Đưa vào model
        model.addAttribute("bill", bill);
        model.addAttribute("billDetails", billDetailsList);
        model.addAttribute("total", total);
        return "admin/billDetail";
    }

    @GetMapping("/history/{id}")
    public String viewBillHistory(@PathVariable("id") Integer billId, Model model) {

        Bill bill = billService.findById(billId);
        model.addAttribute("bill", bill);


        List<BillHistory> histories = billHistoryRepository.findByBillIdOrderByCreatedAtDesc(billId);

        model.addAttribute("bill", bill);
        model.addAttribute("billHistories", histories);;

        return "admin/billHistory";
    }
    @GetMapping("/export/{billId}")
    public void exportInvoice(@PathVariable("billId") Integer billId,
                              HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=hoadon_" + billId + ".pdf");

            Bill bill = billService.findById(billId);

            billService.exportInvoiceToResponse(response, bill);

        } catch (EntityNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

}



