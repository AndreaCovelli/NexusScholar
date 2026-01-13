package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import lombok.Data;

/**
 * DTO containing the data required to create a new Administrator.
 *
 * <p>This class is used exclusively as an input parameter for the creation endpoint. It includes
 * the raw password which must be encrypted by the service layer.
 */
@Data
@Schema(
    description =
        "DTO containing the data required to create a new Administrator. This class is used exclusively as an input parameter.")
public class AdminCreateDTO {

  /** The unique username for the new admin. */
  @Schema(description = "The unique username for the new admin.", example = "admin_master")
  private String username;

  /** The contact email address. */
  @Schema(description = "The contact email address.", example = "admin@university.edu")
  private String email;

  /** The raw password (plaintext). */
  @Schema(description = "The raw password (plaintext).", example = "SecureAdminPass!23")
  private String password;

  /** The initial list of permissions to grant to this admin. */
  @Schema(description = "The initial list of permissions to grant to this admin.")
  private List<Permission> permissions;
}
