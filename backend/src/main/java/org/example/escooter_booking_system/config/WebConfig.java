package org.example.escooter_booking_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply CORS to all paths starting with /api/
                        .allowedOrigins("http://localhost:5173") // Allow your frontend origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true) // Allow credentials (cookies, auth headers)
                        .maxAge(3600); // Cache preflight response for 1 hour

                // Also apply CORS to /pricing/** paths for public pricing API
                registry.addMapping("/pricing/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "OPTIONS") // Public pricing likely only needs GET
                        .allowedHeaders("*")
                        .allowCredentials(false) // Public endpoint likely doesn't need credentials
                        .maxAge(3600);
            }
        };
    }
}