package com.example.quizplatform.config;

import com.example.quizplatform.domain.AppUser;
import com.example.quizplatform.domain.Role;
import com.example.quizplatform.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * Seeds one teacher and one demo student on an empty user table.
 *
 * <p>The defaults are local development credentials. On a public deployment set
 * {@code SEED_TEACHER_PASSWORD} (and optionally the emails) before the first start,
 * because the seeder skips entirely once any user exists.</p>
 */
@Component
@Order(1)
public class UserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);
    private static final String DEFAULT_TEACHER_PASSWORD = "teacher123";
    private static final String DEFAULT_STUDENT_PASSWORD = "student123";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String teacherEmail;
    private final String teacherName;
    private final String teacherPassword;
    private final String studentEmail;
    private final String studentName;
    private final String studentPassword;

    public UserSeeder(AppUserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      Environment environment,
                      @Value("${app.seed.teacher-email:teacher@quizora.local}") String teacherEmail,
                      @Value("${app.seed.teacher-name:Prof. Meera Rao}") String teacherName,
                      @Value("${app.seed.teacher-password:teacher123}") String teacherPassword,
                      @Value("${app.seed.student-email:student@quizora.local}") String studentEmail,
                      @Value("${app.seed.student-name:Demo Student}") String studentName,
                      @Value("${app.seed.student-password:student123}") String studentPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.teacherEmail = teacherEmail;
        this.teacherName = teacherName;
        this.teacherPassword = teacherPassword;
        this.studentEmail = studentEmail;
        this.studentName = studentName;
        this.studentPassword = studentPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        if (isProductionLike() && DEFAULT_TEACHER_PASSWORD.equals(teacherPassword)) {
            log.warn("Seeding the teacher account with the demo password on a non-dev profile. "
                    + "Set SEED_TEACHER_PASSWORD before the first start of a public deployment.");
        }
        userRepository.save(new AppUser(teacherEmail, teacherName,
                passwordEncoder.encode(teacherPassword), Role.TEACHER));
        userRepository.save(new AppUser(studentEmail, studentName,
                passwordEncoder.encode(studentPassword), Role.STUDENT));
        log.info("Seeded demo accounts: {} (TEACHER), {} (STUDENT)", teacherEmail, studentEmail);
    }

    private boolean isProductionLike() {
        return Arrays.stream(environment.getActiveProfiles())
                .noneMatch(profile -> profile.equals("dev") || profile.equals("test"));
    }
}
