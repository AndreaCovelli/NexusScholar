package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO used for updating an existing user's profile. Fields that are null are typically ignored (partial update).")
public class RegisteredUserUpdateDTO {

  /**
   * The new email address. Must not conflict with existing users.
   */
  @Schema(description = "The new email address. Must not conflict with existing users.", example = "new_email@example.com")
  private String email;

  /**
   * The new password. If provided, it will be re-hashed.
   */
  @Schema(description = "The new password. If provided, it will be re-hashed.", example = "NewSecurePass!23")
  private String password;

  /**
   * The updated full name.
   */
  @Schema(description = "The updated full name.", example = "Luigi Verdi")
  private String fullName;

  /**
   * The updated list of bookmarks (optional, usually managed via specific endpoints).
   */
  @Schema(description = "The updated list of bookmarks (optional, usually managed via specific endpoints).")
  private List<BookmarkedPaperDTO> bookmarkedPapers;
}