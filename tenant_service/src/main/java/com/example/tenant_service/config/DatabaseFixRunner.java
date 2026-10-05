package com.example.tenant_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseFixRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Attempting to drop the outdated tenants_status_check constraint...");
            jdbcTemplate.execute("ALTER TABLE tenants DROP CONSTRAINT IF EXISTS tenants_status_check;");
            log.info("Successfully dropped the constraint.");
        } catch (Exception e) {
            log.warn("Could not drop constraint (it might not exist or another issue occurred): {}", e.getMessage());
        }
    }
}
