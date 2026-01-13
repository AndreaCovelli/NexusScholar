package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO used for updating an existing Administrator's profile. Allows changing sensitive information such as the password or permissions.")
public class AdminUpdateDTO {

  /**
   * The new email address.
   */
  @Schema(description = "The new email address.", example = "new_admin@university.edu")
  private String email;

  /**
   * The new password. If provided, it will replace the existing one after hashing.
   */
  @Schema(description = "The new password. If provided, it will replace the existing one after hashing.", example = "NewPass2024!")
  private String password;

  /**
   * The updated list of permissions. Overwrites the previous list.
   */
  @Schema(description = "The updated list of permissions. Overwrites the previous list.")
  private List<Permission> permissions;
}