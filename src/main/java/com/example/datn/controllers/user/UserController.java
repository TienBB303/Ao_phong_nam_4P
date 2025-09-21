package com.example.datn.controllers.user;

import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.services.CustomerService;
import com.example.datn.services.EmailService;
import jakarta.persistence.criteria.Order;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/home")
    public String userHome(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        model.addAttribute("account", account);
        return "user/index";  // dùng index.html làm trang chính
    }

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
    // Method: showProfile(Model model, HttpSession session)
    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfile(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        // Lấy lại Customer với addresses được load sẵn từ database
        Integer customerId = account.getCustomer().getId();
        Customer customerEntity = customerService.findByIdWithAddresses(customerId);
        if (customerEntity == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("account", account);
        CustomerDto customerDto = new CustomerDto();
        if (customerEntity != null) {
            customerDto.setId(customerEntity.getId());
            customerDto.setCode(customerEntity.getCode());
            customerDto.setName(customerEntity.getName());
            customerDto.setGender(customerEntity.getGender());
            customerDto.setBirthday(customerEntity.getBirthDate());
            customerDto.setPhoneNumber(customerEntity.getPhoneNumber());
            customerDto.setEmail(account.getEmail());
            customerDto.setIsActive(customerEntity.getIsActive());
            // Nếu có địa chỉ mặc định
            if (customerEntity.getAddresses() != null && !customerEntity.getAddresses().isEmpty()) {
                // // [Removed] Không chọn địa chỉ mặc định nữa
                // var defaultAddress = customerEntity.getAddresses().stream()
                //     .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                //     .findFirst()
                //     .orElse(customerEntity.getAddresses().get(0));
                var firstAddress = customerEntity.getAddresses().get(0); // lấy địa chỉ đầu tiên
                com.example.datn.dto.AddressDto addressDto = new com.example.datn.dto.AddressDto();
                addressDto.setAddressDetail(firstAddress.getAddressDetail());
                addressDto.setProvinceId(firstAddress.getProvinceId());
                addressDto.setProvinceName(firstAddress.getProvinceName());
                addressDto.setDistrictId(firstAddress.getDistrictId());
                addressDto.setDistrictName(firstAddress.getDistrictName());
                addressDto.setWardId(firstAddress.getWardId());
                addressDto.setWardName(firstAddress.getWardName());
                customerDto.setAddress(addressDto);
            }
        }
        model.addAttribute("customer", customerDto);
        return "user/profile";
    }
   // Hiển thị trang đổi mật khẩu (cho user đã login)
    @GetMapping("/change-password-request")
    public String showChangePasswordRequestPage(HttpSession session, RedirectAttributes redirectAttributes) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập trước khi đổi mật khẩu.");
            return "redirect:/user/login";
        }
        return "user/change-password-request";

    }
    @PostMapping("/change-password-request")
    public String handleChangePasswordRequest(@RequestParam String oldPassword,
                                              @RequestParam String newPassword,
                                              @RequestParam String confirmPassword,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        try {
            Account account = (Account) session.getAttribute("userAccount");
            if (account == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng nhập đã hết hạn.");
                return "redirect:/user/login";
            }

//            // Kiểm tra mật khẩu xác nhận
//            if (!newPassword.equals(confirmPassword)) {
//                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
//                return "redirect:/user/change-password-request";
//            }

            // Kiểm tra độ dài mật khẩu mới
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                return "redirect:/user/change-password-request";
            }

            // Kiểm tra mật khẩu mới không trùng mật khẩu cũ
            if (oldPassword.equals(newPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải khác mật khẩu hiện tại!");
                return "redirect:/user/change-password-request";
            }

            // Kiểm tra mật khẩu xác nhận không được trùng mật khẩu cũ
            if (confirmPassword.equals(oldPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không được trùng với mật khẩu hiện tại!");
                return "redirect:/user/change-password-request";
            }

            // Kiểm tra mật khẩu xác nhận khớp với mật khẩu mới
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/user/change-password-request";
            }
            // Kiểm tra mật khẩu cũ
            if (!customerService.verifyPassword(oldPassword, account.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/user/change-password-request";
            }

            // Cập nhật mật khẩu mới
            customerService.updatePassword(account.getId(), newPassword);

            // Cập nhật lại Account trong session với thông tin mới
            Account updatedAccount = customerService.findAccountById(account.getId());
            if (updatedAccount != null) {
                session.setAttribute("userAccount", updatedAccount);
            }

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đổi mật khẩu thất bại: " + e.getMessage());
            return "redirect:/user/change-password-request";
        }
    }

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

    // Xử lý cập nhật thông tin cá nhân
    @PostMapping("/profile/edit")
    public String editProfile(@ModelAttribute CustomerDto customerDto,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        try {
            // Cập nhật thông tin cá nhân, địa chỉ mặc định
            customerService.updateCustomerProfile(account.getCustomer().getId(), customerDto);
            // Cập nhật lại thông tin trong session
            Customer updatedCustomer = customerService.findById(account.getCustomer().getId());
            account.setCustomer(updatedCustomer);
            session.setAttribute("userCustomer", updatedCustomer);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }
}