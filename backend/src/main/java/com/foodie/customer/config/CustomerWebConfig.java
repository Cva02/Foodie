
package com.foodie.customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomerWebConfig implements WebMvcConfigurer {
    private final CustomerAuthInterceptor auth;
    private final String[] origins;

    public CustomerWebConfig(CustomerAuthInterceptor auth, @Value("${foodie.cors.origins}") String[] origins) {
        this.auth = auth;
        this.origins = origins;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auth)
                .addPathPatterns(
                        "/api/customer/**",
                        "/api/users/**",
                        "/api/notifications/**",
                        "/api/restaurants/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
