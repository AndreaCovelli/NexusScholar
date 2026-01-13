package it.unipi.nexusscholar.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for the authentication response.
 * This class returns the JWT access token to the client upon successful login.
 */
@Data
@AllArgsConstructor
@Schema(description = "Response object containing the JWT authentication token.")
public class AuthResponseDTO {

  /**
   * The JSON Web Token (JWT) issued for the authenticated session.
   */
  @Schema(description = "The JWT access token used for authorizing subsequent requests.", example = "eyJhbGciOiJIUzI1NiJ9...")
  private String accessToken;

  /**
   * The type of the token, typically "Bearer".
   */
  @Schema(description = "The type of the authentication token.", example = "Bearer")
  private String tokenType = "Bearer";

  /**
   * Constructs an AuthResponseDTO with the specified access token.
   * The token type is set to "Bearer" by default.
   *
   * @param accessToken The JWT access token.
   */
  public AuthResponseDTO(String accessToken) {
    this.accessToken = accessToken;
  }
}