package com.adarsh.aiPdfSummerizer.auth.service;

import com.adarsh.aiPdfSummerizer.auth.dto.AuthResponse;
import com.adarsh.aiPdfSummerizer.auth.dto.LoginRequest;
import com.adarsh.aiPdfSummerizer.auth.dto.RegisterRequest;
import com.adarsh.aiPdfSummerizer.common.exception.BadRequestException;
import com.adarsh.aiPdfSummerizer.security.CustomUserDetailsService;
import com.adarsh.aiPdfSummerizer.security.JwtService;
import com.adarsh.aiPdfSummerizer.user.entity.Role;
import com.adarsh.aiPdfSummerizer.user.entity.User;
import com.adarsh.aiPdfSummerizer.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // NEVER store plain text
        // Force-default to CUSTOMER regardless of what client sent, unless
        // explicitly CUSTOMER/SUPPORT_AGENT — never allow self-registration as ADMIN.
        user.setRole(request.getRole() == Role.ADMIN || request.getRole() == null
                ? Role.CUSTOMER
                : request.getRole());

        User saved = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, "Bearer", saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token ,"Bearer", user.getId(),user.getFullName(),user.getEmail(),user.getRole());
    }
}
