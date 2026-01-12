package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;

/**
 * Service interface for handling authentication operations.
 * <p>
 * Defines the contract for logging in different types of users (Admins and Registered Users)
 * and retrieving their authentication tokens.
 * </p>
 */
public interface AuthService {

  /**
   * Authenticates an Admin specifically.
   * <p>
   * Verifies the provided credentials against the admin records and returns a valid
   * JWT token if successful.
   * </p>
   *
   * @param loginRequest The DTO containing the admin's username and password.
   * @return An {@link AuthResponseDTO} containing the generated JWT token.
   * @throws org.springframework.security.authentication.BadCredentialsException If authentication fails.
   */
  AuthResponseDTO loginAdmin(LoginRequestDTO loginRequest);

  /**
   * Authenticates a Registered User specifically.
   * <p>
   * Verifies the provided credentials against the registered user records and returns
   * a valid JWT token if successful.
   * </p>
   *
   * @param loginRequest The DTO containing the user's username and password.
   * @return An {@link AuthResponseDTO} containing the generated JWT token.
   * @throws org.springframework.security.authentication.BadCredentialsException If authentication fails.
   */
  AuthResponseDTO loginRegisteredUser(LoginRequestDTO loginRequest);
}