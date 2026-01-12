package it.unipi.nexusscholar.dto.mongo;

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
public abstract class UserDTO {

  /**
   * The unique MongoDB identifier.
   */
  private String id;

  /**
   * The unique username used for logging in.
   */
  private String username;

  /**
   * The user's contact email address.
   */
  private String email;

  /**
   * The timestamp indicating when the account was created.
   */
  private LocalDateTime createdAt;
}