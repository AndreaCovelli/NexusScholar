package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO representing an Administrator sent in API responses.
 *
 * <p>Extends {@link UserDTO} to include admin-specific fields like permissions. Does not contain
 * sensitive security information like the password hash.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description =
        "DTO representing an Administrator sent in API responses. Extends UserDTO to include admin-specific fields.")
public class AdminDTO extends UserDTO {

  /**
   * The list of permissions granted to this administrator.
   *
   * <p>Uses {@link PermissionDTO} to decouple the API response from the internal model enum.
   */
  @Schema(description = "The list of permissions granted to this administrator.")
  private List<PermissionDTO> permissions;
}
