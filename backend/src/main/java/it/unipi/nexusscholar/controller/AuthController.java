package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;
import it.unipi.nexusscholar.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Dedicated endpoint for Admin login.
     * Path: POST /api/auth/admin/login
     */
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponseDTO> loginAdmin(@RequestBody LoginRequestDTO loginRequest) {
        log.info("Received login request for ADMIN: {}", loginRequest.getUsername());
        return ResponseEntity.ok(authService.loginAdmin(loginRequest));
    }

    /**
     * Dedicated endpoint for Registered User login.
     * Path: POST /api/auth/user/login
     */
    @PostMapping("/user/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@RequestBody LoginRequestDTO loginRequest) {
        log.info("Received login request for USER: {}", loginRequest.getUsername());
        return ResponseEntity.ok(authService.loginRegisteredUser(loginRequest));
    }
}