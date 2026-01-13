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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link AuthService} interface.
 *
 * <p>This class handles the business logic for authentication, including:
 *
 * <ul>
 *   <li>Retrieving user or admin entities from the database.
 *   <li>Verifying raw passwords against stored encrypted passwords.
 *   <li>Generating JWT tokens upon successful authentication using {@link JwtTokenProvider}.
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AdminDAO adminDAO;
  private final RegisteredUserDAO registeredUserDAO;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;

  /**
   * Authenticates an Admin by verifying credentials and generating a token.
   *
   * @param loginRequest The DTO containing the admin's username and password.
   * @return An {@link AuthResponseDTO} with the JWT token.
   * @throws BadCredentialsException If the username is not found or the password is incorrect.
   */
  @Override
  public AuthResponseDTO loginAdmin(LoginRequestDTO loginRequest) {
    String username = loginRequest.getUsername();
    log.info("Attempting ADMIN login for: {}", username);

    // 1. Retrieve Admin
    Admin admin =
        adminDAO
            .findByUsername(username)
            .orElseThrow(
                () -> new BadCredentialsException("Admin not found with username: " + username));

    // 2. Verify Password
    if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
      log.warn("Invalid password for admin: {}", username);
      throw new BadCredentialsException("Invalid credentials");
    }

    // 3. Generate Token with ADMIN role
    String token = jwtTokenProvider.createToken(admin.getUsername(), admin.getId(), "ADMIN");

    return new AuthResponseDTO(token);
  }

  /**
   * Authenticates a Registered User by verifying credentials and generating a token.
   *
   * @param loginRequest The DTO containing the user's username and password.
   * @return An {@link AuthResponseDTO} with the JWT token.
   * @throws BadCredentialsException If the username is not found or the password is incorrect.
   */
  @Override
  public AuthResponseDTO loginRegisteredUser(LoginRequestDTO loginRequest) {
    String username = loginRequest.getUsername();
    log.info("Attempting USER login for: {}", username);

    // 1. Retrieve User
    RegisteredUser user =
        registeredUserDAO
            .findByUsername(username)
            .orElseThrow(
                () -> new BadCredentialsException("User not found with username: " + username));

    // 2. Verify Password
    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      log.warn("Invalid password for user: {}", username);
      throw new BadCredentialsException("Invalid credentials");
    }

    // 3. Generate Token with USER role
    String token = jwtTokenProvider.createToken(user.getUsername(), user.getId(), "USER");

    return new AuthResponseDTO(token);
  }
}
