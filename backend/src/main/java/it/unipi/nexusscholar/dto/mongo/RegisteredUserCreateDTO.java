package it.unipi.nexusscholar.dto.mongo;

import lombok.Data;

/**
 * DTO containing the data required to register a new user.
 * <p>
 * This class is used exclusively as an input parameter for the registration endpoint.
 * It includes sensitive information like the raw password, which will be encrypted
 * by the service layer before storage.
 * </p>
 */
@Data
public class RegisteredUserCreateDTO {

  /**
   * The desired unique username.
   */
  private String username;

  /**
   * A valid email address.
   */
  private String email;

  /**
   * The raw password provided by the user (plaintext).
   */
  private String password;

  /**
   * The user's full legal name or display name.
   */
  private String fullName;
}