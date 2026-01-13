package it.unipi.nexusscholar.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing an Administrator in the system.
 *
 * <p>This class extends the base {@link User} class to include administrative privileges. Unlike
 * standard users, admins possess specific permissions to manage platform content and user accounts.
 *
 * <p>Data is stored in the separate "admins" collection for security segregation.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admins")
public class Admin extends User {

  /**
   * A list of specific granular permissions granted to this administrator.
   *
   * <p>Examples include {@link Permission#DELETE_PAPER} or {@link Permission#BAN_USER}.
   */
  private List<Permission> permissions;
}
