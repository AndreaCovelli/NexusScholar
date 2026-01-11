package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;
import it.unipi.nexusscholar.model.mongo.Admin;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminDAO adminDAO;
    private final RegisteredUserDAO registeredUserDAO;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO loginAdmin(LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();
        log.info("Attempting ADMIN login for: {}", username);

        // 1. Retrieve Admin
        Admin admin = adminDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found with username: " + username));

        // 2. Verify Password
        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            log.warn("Invalid password for admin: {}", username);
            throw new BadCredentialsException("Invalid credentials");
        }

        // 3. Generate Token with ADMIN role
        String token = jwtTokenProvider.createToken(
                admin.getUsername(),
                admin.getId(),
                "ADMIN"
        );

        return new AuthResponseDTO(token);
    }

    @Override
    public AuthResponseDTO loginRegisteredUser(LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();
        log.info("Attempting USER login for: {}", username);

        // 1. Retrieve User
        RegisteredUser user = registeredUserDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. Verify Password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid credentials");
        }

        // 3. Generate Token with USER role
        String token = jwtTokenProvider.createToken(
                user.getUsername(),
                user.getId(),
                "USER"
        );

        return new AuthResponseDTO(token);
    }
}