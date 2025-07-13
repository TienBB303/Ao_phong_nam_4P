
package com.example.datn.controllers;

import com.example.datn.entities.Bill;
import com.example.datn.repositories.BillRepository;
import com.example.datn.services.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}

