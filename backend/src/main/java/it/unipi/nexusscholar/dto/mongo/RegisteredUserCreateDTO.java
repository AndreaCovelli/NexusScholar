package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO containing the data required to register a new user.
 *
 * <p>This class is used exclusively as an input parameter for the registration endpoint. It
 * includes sensitive information like the raw password, which will be encrypted by the service
 * layer before storage.
 */
@Data
@Schema(
    description =
        "DTO containing the data required to register a new user. Used as input for registration.")
public class RegisteredUserCreateDTO {

  /** The desired unique username. */
  @Schema(description = "The desired unique username.", example = "scholar_fan_99")
  private String username;

  /** A valid email address. */
  @Schema(description = "A valid email address.", example = "student@university.edu")
  private String email;

  /** The raw password provided by the user (plaintext). */
  @Schema(
      description = "The raw password provided by the user (plaintext).",
      example = "SecretPass123!")
  private String password;

  /** The user's full legal name or display name. */
  @Schema(description = "The user's full legal name or display name.", example = "Mario Rossi")
  private String fullName;
}
