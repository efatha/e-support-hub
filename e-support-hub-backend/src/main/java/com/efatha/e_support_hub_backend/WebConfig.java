package com.efatha.e_support_hub_backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "https://e-support-hub.vercel.app",
                "http://localhost",
                "http://localhost:5173"
            )
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}