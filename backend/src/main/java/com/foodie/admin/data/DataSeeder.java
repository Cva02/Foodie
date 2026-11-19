package com.foodie.admin.data;

import com.foodie.admin.model.Admin;
import com.foodie.admin.repository.AdminRepository;
import com.foodie.admin.repository.RiderRepository;
import com.foodie.admin.repository.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Runs on every start, but only writes to MySQL when a table is still empty. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminRepository admins;
    private final SellerRepository sellers;
    private final RiderRepository riders;
    private final DummyData demo;
    private final PasswordEncoder encoder;
    private final String adminUser;
    private final String adminPassword;
    private final boolean seedDemo;

    public DataSeeder(AdminRepository admins, SellerRepository sellers, RiderRepository riders,
                      DummyData demo, PasswordEncoder encoder,
                      @Value("${foodie.admin.username}") String adminUser,
                      @Value("${foodie.admin.password}") String adminPassword,
                      @Value("${foodie.seed-demo-data:true}") boolean seedDemo) {
        this.admins = admins;
        this.sellers = sellers;
        this.riders = riders;
        this.demo = demo;
        this.encoder = encoder;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.seedDemo = seedDemo;
    }

    @Override
    public void run(String... args) {
        // The one and only admin account – created once, never by an endpoint.
        if (admins.count() == 0) {
            admins.save(new Admin(adminUser, encoder.encode(adminPassword)));
            log.info("Created the admin account '{}'", adminUser);
        }

        if (seedDemo && sellers.count() == 0 && riders.count() == 0) {
            sellers.saveAll(demo.sellers.values());
            riders.saveAll(demo.riders.values());
            log.info("Loaded {} demo sellers and {} demo riders into MySQL",
                    demo.sellers.size(), demo.riders.size());
        }
    }
}
