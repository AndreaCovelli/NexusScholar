package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;
import it.unipi.nexusscholar.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling authentication requests.
 *
 * <p>This controller exposes public endpoints for logging in Administrators and Registered Users.
 * Upon successful authentication, it returns a JWT token that must be included in subsequent
 * requests to protected resources.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication API",
    description = "Endpoints for managing user and admin login sessions")
public class AuthController {

  private final AuthService authService;

  /**
   * Authenticates an Administrator.
   *
   * <p>Accepts admin credentials, validates them against the database, and returns a JWT token with
   * ADMIN role permissions.
   *
   * @param loginRequest The DTO containing the admin's username and password.
   * @return A {@link ResponseEntity} containing the JWT token if successful.
   */
  @Operation(
      summary = "Admin Login",
      description =
          "Authenticates an administrator using username and password. Returns a JWT token with ADMIN privileges.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful, token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "400", description = "Malformed request body")
      })
  @PostMapping("/admin/login")
  public ResponseEntity<AuthResponseDTO> loginAdmin(@RequestBody LoginRequestDTO loginRequest) {
    log.info("Received login request for ADMIN: {}", loginRequest.getUsername());
    return ResponseEntity.ok(authService.loginAdmin(loginRequest));
  }

  /**
   * Authenticates a Registered User.
   *
   * <p>Accepts user credentials, validates them against the database, and returns a JWT token with
   * USER role permissions.
   *
   * @param loginRequest The DTO containing the user's username and password.
   * @return A {@link ResponseEntity} containing the JWT token if successful.
   */
  @Operation(
      summary = "User Login",
      description =
          "Authenticates a registered user using username and password. Returns a JWT token with standard USER privileges.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful, token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "400", description = "Malformed request body")
      })
  @PostMapping("/user/login")
  public ResponseEntity<AuthResponseDTO> loginUser(@RequestBody LoginRequestDTO loginRequest) {
    log.info("Received login request for USER: {}", loginRequest.getUsername());
    return ResponseEntity.ok(authService.loginRegisteredUser(loginRequest));
  }
}
