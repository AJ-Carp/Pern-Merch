package com.ajcarpinello.Pern_Merch_Website.config;

import com.ajcarpinello.Pern_Merch_Website.entity.Role;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

// creates admin account if dosen't exist

@Component
@RequiredArgsConstructor
@Order(1)
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.secret.username}")
    private String adminUsername;

    @Value("${admin.secret.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            userRepository.save(User.builder()
                    .username(adminUsername)
                    .email("admin@pernband.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build());
            System.out.println(">>> Seeded admin user");
        }
    }
}
