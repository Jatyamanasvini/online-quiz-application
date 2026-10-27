package com.example.quizplatform.config;

import com.example.quizplatform.domain.AppUser;
import com.example.quizplatform.domain.Role;
import com.example.quizplatform.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds one teacher and one demo student on an empty user table.
 * Demo credentials are documented in the README for local use only.
 */
@Component
@Order(1)
public class UserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new AppUser("teacher@quizora.local", "Prof. Meera Rao",
                passwordEncoder.encode("teacher123"), Role.TEACHER));
        userRepository.save(new AppUser("student@quizora.local", "Demo Student",
                passwordEncoder.encode("student123"), Role.STUDENT));
        log.info("Seeded demo users: teacher@quizora.local (TEACHER), student@quizora.local (STUDENT)");
    }
}
