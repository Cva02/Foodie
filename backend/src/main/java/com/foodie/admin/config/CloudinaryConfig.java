
package com.foodie.admin.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "foodie.storage.cloudinary",
            name = "enabled",
            havingValue = "true"
    )
    public Cloudinary cloudinary(
            @Value("${foodie.storage.cloudinary.cloud-name:}") String cloudName,
            @Value("${foodie.storage.cloudinary.api-key:}") String apiKey,
            @Value("${foodie.storage.cloudinary.api-secret:}") String apiSecret) {

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Cloudinary is enabled but its credentials are missing"
            );
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}
