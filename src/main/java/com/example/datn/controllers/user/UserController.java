package com.example.datn.controllers.user;

import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.services.CustomerService;
import com.example.datn.services.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailService emailService;

    // Hiển thị trang đăng ký
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        CustomerDto customerDto = new CustomerDto();
        model.addAttribute("customerDto", customerDto);
        return "user/signup";
    }

    // Xử lý đăng ký
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute CustomerDto customerDto, 
                               RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (customerService.existsByEmail(customerDto.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng. Vui lòng chọn email khác.");
                return "redirect:/user/signup";
            }

            // Tạo khách hàng mới với tài khoản
            Customer customer = customerService.createCustomerWithAddressAndAccount(customerDto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đăng ký thành công! Vui lòng kiểm tra email để lấy thông tin đăng nhập.");
            
            return "redirect:/user/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/user/signup";
        }
    }

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "user/login";
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String processLogin(@RequestParam String email, 
                              @RequestParam String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            // Xác thực người dùng
            Account account = customerService.authenticateUser(email, password);
            
            if (account != null) {
                // Lưu thông tin người dùng vào session
                session.setAttribute("userAccount", account);
                session.setAttribute("userCustomer", account.getCustomer());
                session.setMaxInactiveInterval(15 * 60);
                
                // Kiểm tra xem có phải lần đầu đăng nhập không
                if (account.getStatus() == null || !account.getStatus()) {
                    // Chuyển đến trang đổi mật khẩu
                    return "redirect:/user/change-password";
                }
                
                redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");
                return "redirect:/";
            } else {
                redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng.");
                return "redirect:/user/login";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đăng nhập thất bại: " + e.getMessage());
            return "redirect:/user/login";
        }
    }

    // Hiển thị trang đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        return "user/change-password";
    }

    // Xử lý đổi mật khẩu
    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String currentPassword,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        try {
            Account account = (Account) session.getAttribute("userAccount");
            if (account == null) {
                return "redirect:/user/login";
            }

            // Kiểm tra mật khẩu hiện tại
            if (!customerService.verifyPassword(currentPassword, account.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
                return "redirect:/user/change-password";
            }

            // Kiểm tra mật khẩu mới
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
                return "redirect:/user/change-password";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                return "redirect:/user/change-password";
            }

            // Cập nhật mật khẩu
            customerService.updatePassword(account.getId(), newPassword);
            
            // Cập nhật trạng thái tài khoản
            customerService.updateAccountStatus(account.getId(), true);
            
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đổi mật khẩu thất bại: " + e.getMessage());
            return "redirect:/user/change-password";
        }
    }

    // Hiển thị trang profile
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("account", account);
        model.addAttribute("customer", account.getCustomer());
        return "user/profile";
    }

    // Hiển thị trang đơn hàng
    @GetMapping("/orders")
    public String showOrders(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        // TODO: Implement order listing logic
        model.addAttribute("customer", account.getCustomer());
        return "user/orders";
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/";
    }
} 