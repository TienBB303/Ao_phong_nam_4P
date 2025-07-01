package com.example.datn;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.spring6.SpringTemplateEngine;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // First comment TienBB
        // Second comment TienBB
        System.out.println("Build complete!");
    }

//    @Bean
//    public SpringTemplateEngine templateEngine() {
//        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//        templateEngine.addDialect(new LayoutDialect());
//        return templateEngine;
//    }
}
