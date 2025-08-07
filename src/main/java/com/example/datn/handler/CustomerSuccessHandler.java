package com.example.datn.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomerSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String redirectUrl = "/";

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_EMPLOYEE")) {
                redirectUrl = "/admin";
                break;
            } else if (auth.getAuthority().equals("ROLE_CUSTOMER")) {
                redirectUrl = "";
                break;
            }
        }
        response.sendRedirect(redirectUrl);
    }
}
