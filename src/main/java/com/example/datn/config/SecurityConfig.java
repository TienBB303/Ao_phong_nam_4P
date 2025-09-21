package com.example.datn.config;

import com.example.datn.services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.example.datn.config.CustomAuthenticationFailureHandler;
import com.example.datn.config.UserLoginSuccessHandler;
//import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
public class SecurityConfig {

    @Autowired
    private LoginService loginService;
    @Autowired
    private UserLoginSuccessHandler userLoginSuccessHandler;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(loginService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/fonts/**",
                                "/static/**", "/webjars/**", "/assets/**"
                        ).permitAll()
                        .requestMatchers("/login", "/register", "/user/signup").permitAll()
                        // Chỉ Admin được phép xem trang & gọi API doanh thu
                        .requestMatchers("/admin/revenue", "/admin/revenue/", "/admin/revenue/api/**").hasAuthority("ROLE_ADMIN")
                        // ADMIN và EMPLOYEE đều truy cập được dashboard, bán hàng, hóa đơn
                        .requestMatchers("/admin/dashboard", "/admin/sell-inline/**", "/admin/sell-inline", "/admin/bill/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                        .requestMatchers("/admin/**", "/all-admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/user/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_EMPLOYEE", "ROLE_ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(userLoginSuccessHandler)
//                        .failureUrl("/login?error=true")
                                .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login?expired")
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/login?access-denied")
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/login?session-expired");
                        })
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return loginService;
    }
    // ✅ Kích hoạt SpringSecurityDialect để #authentication và sec:authorize hoạt động
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
