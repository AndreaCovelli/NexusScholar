package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Abstract Data Transfer Object representing the common attributes of any user in the system.
 * <p>
 * Serves as a base class for specific user types (e.g., RegisteredUser, Admin)
 * to ensure consistent field naming for ID, username, and email in API responses.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Abstract Data Transfer Object representing the common attributes of any user in the system.")
public abstract class UserDTO {

  /**
   * The unique MongoDB identifier.
   */
  @Schema(description = "The unique MongoDB identifier.", example = "507f1f77bcf86cd799439011")
  private String id;

  /**
   * The unique username used for logging in.
   */
  @Schema(description = "The unique username used for logging in.", example = "user123")
  private String username;

  /**
   * The user's contact email address.
   */
  @Schema(description = "The user's contact email address.", example = "user@example.com")
  private String email;

  /**
   * The timestamp indicating when the account was created.
   */
  @Schema(description = "The timestamp indicating when the account was created.", example = "2023-01-15T10:00:00")
  private LocalDateTime createdAt;
}