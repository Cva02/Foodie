
package com.foodie.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
    scanBasePackages = {
        "com.foodie.admin",
        "com.foodie.customer"
    }
)
@EntityScan(basePackages = {
    "com.foodie.admin.model",
    "com.foodie.customer.model"
})
@EnableJpaRepositories(
    basePackages = {
        "com.foodie.admin.repository",
        "com.foodie.customer.repo"
    },
    considerNestedRepositories = true
)
@EnableScheduling
public class FoodieAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodieAdminApplication.class, args);
    }
}
