package com.foodie.admin.rider.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RiderWebConfig implements WebMvcConfigurer {

    private final RiderAuthInterceptor riderAuthInterceptor;

    public RiderWebConfig(RiderAuthInterceptor riderAuthInterceptor) {
        this.riderAuthInterceptor = riderAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(riderAuthInterceptor)
                .addPathPatterns("/api/rider/**")
                .excludePathPatterns("/api/rider/auth/login");
    }
}