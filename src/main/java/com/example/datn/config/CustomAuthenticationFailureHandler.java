package com.example.datn.config;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String errorCode = "bad_credentials";
        if (exception instanceof DisabledException) {
            errorCode = "disabled";
        } else if (exception instanceof LockedException) {
            errorCode = "locked";
        } else if (exception instanceof AccountExpiredException) {
            errorCode = "account_expired";
        } else if (exception instanceof CredentialsExpiredException) {
            errorCode = "credentials_expired";
        } else if (exception instanceof UsernameNotFoundException) {
            errorCode = "account_not_found";
        } else if (exception instanceof BadCredentialsException) {
            errorCode = "bad_credentials";
        }

        response.sendRedirect("/login?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    }

}
