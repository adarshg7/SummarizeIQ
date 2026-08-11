package com.adarsh.aiPdfSummerizer.auth.controller;

import com.adarsh.aiPdfSummerizer.auth.dto.AuthResponse;
import com.adarsh.aiPdfSummerizer.auth.dto.LoginRequest;
import com.adarsh.aiPdfSummerizer.auth.dto.RegisterRequest;
import com.adarsh.aiPdfSummerizer.auth.service.AuthService;
import com.adarsh.aiPdfSummerizer.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully",response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request){
        AuthResponse response = authService.login(request);
        return  ResponseEntity.ok(ApiResponse.success("Login successful",response));
    }
}
