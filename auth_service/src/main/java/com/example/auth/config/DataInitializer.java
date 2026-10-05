package com.example.auth.config;

import com.example.auth.entity.RoleEntity;
import com.example.auth.entity.UserEntity;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed Admin
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setRole(RoleEntity.ADMIN);
            admin.setIsActive(true);
            admin.setIsVerified(true);
            userRepository.save(admin);
            log.info(">>> Admin account created: admin@gmail.com");
        } else {
            UserEntity existingAdmin = userRepository.findByEmail("admin@gmail.com").orElse(null);
            if (existingAdmin != null && existingAdmin.getRole() != RoleEntity.ADMIN) {
                existingAdmin.setRole(RoleEntity.ADMIN);
                userRepository.save(existingAdmin);
                log.info(">>> Admin account updated to ADMIN role: admin@gmail.com");
            } else {
                log.info(">>> Admin account already exists and has correct role, skipping.");
            }
        }
    }
}
