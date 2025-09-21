package com.example.datn.controllers.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
//            model.addAttribute("error", "Sai email hoặc mật khẩu.");
            String message;
            switch (error) {
                case "disabled" -> message = "Tài khoản bị khóa";
                case "bad_credentials" -> message = "Sai email hoặc mật khẩu.";
                case "account_not_found" -> message = "Email không tồn tại.";
                case "locked" -> message = "Tài khoản đang bị khóa. Vui lòng liên hệ CSKH.";
                default -> message = "Đăng nhập thất bại. Vui lòng thử lại.";
            }
            model.addAttribute("error", message);
        }
        return "user/login";
    }
}
