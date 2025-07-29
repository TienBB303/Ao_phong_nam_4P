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
            }
        }
        return "user/tracking-order";
    }

}
