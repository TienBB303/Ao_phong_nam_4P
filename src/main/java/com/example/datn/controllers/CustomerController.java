package com.example.datn.controllers;
//import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
import com.example.datn.entities.Discount;
import com.example.datn.repositories.CustomerRepository;
import com.example.datn.repositories.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/customer")
public class CustomerController {
    @Autowired
    CustomerRepository customerRepo;
    // Hiển thị danh sách khách hàng có tìm kiếm + phân trang
    @GetMapping("/view")
    public String viewCustomers(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String keyword) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Customer> customerPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            customerPage = customerRepo.searchCustomerKeyword(keyword, pageable);
        } else {
            customerPage = customerRepo.findByIsActiveTrue(pageable);
        }

        model.addAttribute("customerPage", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/customer/customerList";
    }

    // Hiển thị form tạo khách hàng mới
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("customer", new Customer());
        return "admin/customer/customerCreate1.html";
    }


    // Xử lý lưu khách hàng
    @PostMapping("/save")
    public String save(@ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        // Kiểm tra trùng mã khi tạo mới
        if (customer.getId() == null && customerRepo.existsByCode(customer.getCode())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã khách hàng đã tồn tại. Vui lòng chọn mã khác.");
            return "redirect:/admin/customer/create";
        }

        customerRepo.save(customer);
        redirectAttributes.addFlashAttribute("message", "Lưu khách hàng thành công!");
        return "redirect:/admin/customer/view";
    }

    // Hiển thị form chỉnh sửa khách hàng
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOptional = customerRepo.findById(id);
        if (customerOptional.isPresent()) {
            model.addAttribute("customer", customerOptional.get());
            return "admin/customer/customerEdit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại!");
            return "redirect:/admin/customer/view";
        }
    }
    @PostMapping("/update")
    public String updateCustomer(@ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        if (!customerRepo.existsById(customer.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng để cập nhật!");
            return "redirect:/admin/customer/view";
        }
        customer.setIsActive(true);
        customerRepo.save(customer); // lưu thay đổi
        redirectAttributes.addFlashAttribute("message", "Cập nhật khách hàng thành công!");
        return "redirect:/admin/customer/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Integer id, RedirectAttributes redirect) {
        Optional<Customer> customerOpt = customerRepo.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setIsActive(false); // cập nhật trạng thái xóa mềm
            customerRepo.save(customer); // lưu lại vào DB
            redirect.addFlashAttribute("success", "Xóa khách hàng thành công (dạng ẩn)");
        } else {
            redirect.addFlashAttribute("error", "Không tìm thấy khách hàng.");
        }
        return "redirect:/admin/customer/view"; // điều hướng lại danh sách
    }

//    @GetMapping("/search")
//    public String searchCustomers(@RequestParam("keyword") String keyword,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  Model model) {
//        int pageSize = 5;
//        Pageable pageable = PageRequest.of(page, pageSize);
//
//        Page<CustomerDto> searchResults = customerRepo.searchCustomerKeyword(keyword, pageable);
//        model.addAttribute("customerPage", searchResults);
//        // Sử dụng cùng tên attribute để tái sử dụng view
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", searchResults.getTotalPages());
//        model.addAttribute("keyword", keyword); // Giữ lại keyword để hiển thị trên form tìm kiếm
//
//        return "admin/customer/customerList"; // Hiển thị kết quả tìm kiếm trên cùng trang view
//    }
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model) {
        Customer customer = customerRepo.findById(id).orElse(null);
        model.addAttribute("customer", customer);
        return "admin/customer/customerDetail";
    }
}
