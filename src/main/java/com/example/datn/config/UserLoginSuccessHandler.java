package com.example.datn.config;

import com.example.datn.entities.Account;
import com.example.datn.repositories.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private AccountRepository accountRepository;

//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        String email = authentication.getName();
//        Account account = accountRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//
//        HttpSession session = request.getSession();
//        session.setAttribute("userAccount", account);
//
//        if (account.getCustomer() != null) {
//            session.setAttribute("userCustomer", account.getCustomer());
//        }
//
//        // Lưu role vào session để phân quyền hiển thị trên giao diện
//        for (GrantedAuthority auth : authentication.getAuthorities()) {
//            session.setAttribute("userRole", auth.getAuthority());
//        }
//
//        // ✅ Tất cả role sau login đều vào /
//        // Nếu là ADMIN hoặc EMPLOYEE thì chuyển hướng về trang quản lý
//        if (account.getRole().getName().equals("ROLE_ADMIN") || account.getRole().getName().equals("ROLE_EMPLOYEE")) {
//            response.sendRedirect("/admin");
//            return;
//        }
//        // Nếu là CUSTOMER thì chuyển hướng về trang người dùng
//        response.sendRedirect("/user/home");
//
//    }
@Override
public void onAuthenticationSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {

    HttpSession session = request.getSession();

    // Lấy account từ DB theo email login
    String email = authentication.getName();
    Account account = accountRepository.findByEmail(email).orElse(null);

    if (account != null) {
        session.setAttribute("userAccount", account);
        session.setAttribute("userCustomer", account.getCustomer());
        // Lưu role vào session để phân quyền giao diện
        String roleName = account.getRole().getName();
        session.setAttribute("userRole", roleName);
    }

    // ✅ Log ra quyền thực tế để kiểm tra
    System.out.println("👉 Authorities after login: " + authentication.getAuthorities());

    // Redirect về trang chủ sau khi login thành công
    response.sendRedirect("/");
}
//    HttpSession session = request.getSession();
//
//    // Lấy account từ DB theo email login
//    String email = authentication.getName();
//    Account account = accountRepository.findByEmail(email).orElse(null);
//
//    if (account != null) {
//        session.setAttribute("userAccount", account);
//        session.setAttribute("userCustomer", account.getCustomer());
//
//        // Lưu roleName vào session để Thymeleaf dùng
//        String roleName = account.getRole().getName();
//        session.setAttribute("userRole", roleName);
//
//        System.out.println("✅ Login success - userRole = " + roleName);
//    }
//
//    // ✅ Authorities debug
//    System.out.println("👉 Authorities after login: " + authentication.getAuthorities());
//
//    // Điều hướng theo role
//    String role = (account != null) ? account.getRole().getName() : "";
//
//    if ("ROLE_ADMIN".equals(role)) {
//        response.sendRedirect("/admin/dashboard");
//    } else if ("ROLE_EMPLOYEE".equals(role)) {
//        response.sendRedirect("/admin/sell-inline/hien-thi");
//    } else {
//        response.sendRedirect("/user/home");
//    }
}
