package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import lombok.Data;

/**
 * DTO containing the data required to create a new Administrator.
 * <p>
 * This class is used exclusively as an input parameter for the creation endpoint.
 * It includes the raw password which must be encrypted by the service layer.
 * </p>
 */
@Data
public class AdminCreateDTO {

  /**
   * The unique username for the new admin.
   */
  private String username;

  /**
   * The contact email address.
   */
  private String email;

  /**
   * The raw password (plaintext).
   */
  private String password;

  /**
   * The initial list of permissions to grant to this admin.
   */
  private List<Permission> permissions;
}