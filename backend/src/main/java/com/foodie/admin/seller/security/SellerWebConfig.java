
package com.foodie.admin.seller.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SellerWebConfig implements WebMvcConfigurer {

    private final SellerAuthInterceptor sellerAuthInterceptor;

    public SellerWebConfig(
            SellerAuthInterceptor sellerAuthInterceptor) {
        this.sellerAuthInterceptor = sellerAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sellerAuthInterceptor)
                .addPathPatterns("/api/seller/**")
                .excludePathPatterns("/api/seller/auth/login");
    }
}
