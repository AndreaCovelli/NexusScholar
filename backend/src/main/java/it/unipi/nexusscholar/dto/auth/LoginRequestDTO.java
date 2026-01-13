package it.unipi.nexusscholar.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for handling user login requests.
 * This class captures the credentials required for authentication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for user authentication containing username and password.")
public class LoginRequestDTO {

  /**
   * The username of the user attempting to log in.
   */
  @Schema(description = "The unique username of the user.", example = "john_doe")
  private String username;

  /**
   * The password associated with the user's account.
   */
  @Schema(description = "The password of the user.", example = "SecurePass123!")
  private String password;
}