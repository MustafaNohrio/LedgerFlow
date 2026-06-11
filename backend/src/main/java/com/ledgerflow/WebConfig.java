package com.ledgerflow;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

// CORS (Cross-Origin Resource Sharing) configuration.
// The frontend runs from a file:// or a different port than the backend (localhost:8080).
// Without this config, the browser would block all API calls from the frontend
// because of the "Same-Origin Policy" security rule.
// This tells the backend: "it's okay, let any origin call our /api/ endpoints."
// It also redirects the root URL (/) to the login page.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")               // Allow requests from any origin
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);                   // Allow cookies/sessions to be sent
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect http://localhost:8080/ → http://localhost:8080/login.html
        registry.addRedirectViewController("/", "/login.html");
    }
}
