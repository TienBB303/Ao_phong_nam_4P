package com.example.datn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootApplication
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner testMailConfiguration(@Autowired JavaMailSender mailSender) {
        return args -> {
            logger.info("=== KIỂM TRA CẤU HÌNH MAIL ===");
            logger.info("Mail sender class: {}", mailSender.getClass().getSimpleName());

            if (mailSender instanceof JavaMailSenderImpl impl) {
                logger.info("Mail sender host: {}", impl.getHost());
                logger.info("Mail sender port: {}", impl.getPort());
                logger.info("Mail sender username: {}", impl.getUsername());
                logger.info("Mail sender password: {}", impl.getPassword() != null ? "***SET***" : "NULL");
            } else {
                logger.warn("mailSender không phải là JavaMailSenderImpl, không thể kiểm tra cấu hình chi tiết.");
            }
            logger.info("=== KẾT THÚC KIỂM TRA ===");
        };
    }
}
