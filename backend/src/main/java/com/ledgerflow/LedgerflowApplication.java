package com.ledgerflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// Main entry point for the LedgerFlow application.
// This is the file that starts everything when you run "mvn spring-boot:run".
//
// We exclude SecurityAutoConfiguration because we handle authentication ourselves
// using SessionHelper instead of Spring Security's built-in form login.
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class LedgerflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(LedgerflowApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("  LedgerFlow running at http://localhost:8080");
        System.out.println("  Open frontend/login.html to get started");
        System.out.println("==============================================\n");
    }
}
