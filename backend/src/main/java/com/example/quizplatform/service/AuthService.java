package com.example.quizplatform.service;

import com.example.quizplatform.domain.AppUser;
import com.example.quizplatform.domain.Role;
import com.example.quizplatform.dto.AuthResponse;
import com.example.quizplatform.dto.LoginRequest;
import com.example.quizplatform.dto.RegisterRequest;
import com.example.quizplatform.dto.UserView;
import com.example.quizplatform.exception.ConflictException;
import com.example.quizplatform.exception.UnauthorizedException;
import com.example.quizplatform.repository.AppUserRepository;
import com.example.quizplatform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        AppUser user = userRepository.save(new AppUser(
                email,
                request.name().trim(),
                passwordEncoder.encode(request.password()),
                Role.STUDENT));
        return new AuthResponse(jwtService.generate(user), toView(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return new AuthResponse(jwtService.generate(user), toView(user));
    }

    @Transactional(readOnly = true)
    public UserView me(String email) {
        return toView(userByEmail(email));
    }

    @Transactional(readOnly = true)
    public AppUser userByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("This account no longer exists"));
    }

    public static UserView toView(AppUser user) {
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
