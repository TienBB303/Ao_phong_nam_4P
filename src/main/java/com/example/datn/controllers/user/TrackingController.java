package com.example.datn.controllers.user;

import com.example.datn.entities.Bill;
import com.example.datn.entities.BillDetails;
import com.example.datn.services.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class TrackingController {

    @Autowired
    BillService billService;

    @GetMapping("/tracking")
    public String trackingBill(@RequestParam(name = "code", required = false) String code, Model model) {
        if (code != null && !code.isBlank()) {
            Boolean targetTypeBill = true;

            Bill bill = billService.findByCodeAndTypeBill(code.trim(), targetTypeBill);
            if (bill != null) {
                model.addAttribute("bill", bill);


                List<BillDetails> billDetailsList = bill.getBillDetails();

                BigDecimal total = BigDecimal.ZERO;
                if (billDetailsList != null) {
                    for (BillDetails detail : billDetailsList) {
                        if (detail.getPrice() != null && detail.getQuantity() != null) {
                            total = total.add(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
                        }
                    }
                }

                model.addAttribute("billDetails", billDetailsList);
                model.addAttribute("total", total);

            } else {
                model.addAttribute("notFound", true);
                model.addAttribute("searchCode", code);
                model.addAttribute("bill", null);
            }
        }
        return "user/tracking-order";
    }


    @GetMapping("/update-bill-status/{billId}")
    public String updateBillStatus(@PathVariable Integer billId,
                                   @RequestParam("trangThaiDonHang") String trangThaiDonHang,
                                   @RequestParam("code") String code,
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
                return "redirect:/tracking?code=" + code;
            }

            Bill bill = billService.getOne(billId);
            if (bill.getStatus() != 1 && bill.getStatus() != 2) {
                redirectAttributes.addFlashAttribute("error",
                        "Huỷ đơn hàng thất bại");
                return "redirect:/tracking?code=" + code;
            }

            // Cập nhật trạng thái
            billService.updateStatus(status, billId); // TienBB sửa trangThaiDonHang thành status đã ép kiểu
            redirectAttributes.addFlashAttribute("message",
                    "Bạn đã huỷ đơn hàng " + bill.getCode() + " thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi huỷ đơn hàng.");
        }

        return "redirect:/tracking?code=" + code;

    }

}