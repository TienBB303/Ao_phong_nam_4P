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

    // Lấy account theo email đăng nhập
    String email = authentication.getName();
    Account account = accountRepository.findByEmail(email).orElse(null);

    if (account != null) {
        session.setAttribute("userAccount", account);
        session.setAttribute("userCustomer", account.getCustomer());
        String roleName = (account.getRole() != null) ? account.getRole().getName() : null;
        session.setAttribute("userRole", roleName);

        // Chỉ ép đổi mật khẩu cho CUSTOMER
        if ("ROLE_CUSTOMER".equals(roleName)) {
            Boolean status = account.getStatus();
            boolean needChangePassword = (status == null || !status);
            if (needChangePassword) {
                response.sendRedirect("/user/change-password");
                return;
            }
        }

        // Điều hướng theo role sau khi đã đổi mật khẩu (hoặc không phải CUSTOMER)
        if ("ROLE_ADMIN".equals(roleName) || "ROLE_EMPLOYEE".equals(roleName)) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        response.sendRedirect("/");
        return;
    }

    // Trường hợp không tìm thấy account (rất hiếm)
    response.sendRedirect("/login");
}


}