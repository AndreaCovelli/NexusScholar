package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO representing an Administrator sent in API responses.
 * <p>
 * Extends {@link UserDTO} to include admin-specific fields like permissions.
 * Does not contain sensitive security information like the password hash.
 * </p>
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO extends UserDTO {

  /**
   * The list of permissions granted to this administrator.
   * <p>
   * Uses {@link PermissionDTO} to decouple the API response from the internal model enum.
   * </p>
   */
  private List<PermissionDTO> permissions;
}