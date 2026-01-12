package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO used for updating an existing user's profile.
 * <p>
 * Contains fields that are modifiable by the user. Fields that are null
 * in the request are typically ignored by the service (partial update) or
 * handled according to specific business rules.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredUserUpdateDTO {

  /**
   * The new email address. Must not conflict with existing users.
   */
  private String email;

  /**
   * The new password. If provided, it will be re-hashed.
   */
  private String password;

  /**
   * The updated full name.
   */
  private String fullName;

  /**
   * The updated list of bookmarks (optional, usually managed via specific endpoints).
   */
  private List<BookmarkedPaperDTO> bookmarkedPapers;
}