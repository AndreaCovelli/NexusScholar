package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import lombok.Data;

/**
 * DTO used for updating an existing Administrator's profile.
 * <p>
 * Allows changing sensitive information such as the password or the assigned permissions.
 * </p>
 */
@Data
public class AdminUpdateDTO {

  /**
   * The new email address.
   */
  private String email;

  /**
   * The new password. If provided, it will replace the existing one after hashing.
   */
  private String password;

  /**
   * The updated list of permissions. Overwrites the previous list.
   */
  private List<Permission> permissions;
}