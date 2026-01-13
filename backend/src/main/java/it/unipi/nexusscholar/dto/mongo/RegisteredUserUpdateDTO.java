package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating an existing user's profile.
 * <p>
 * This class encapsulates the fields that a user is allowed to modify. Fields that are null
 * in the request indicate that no change is requested for that specific property.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO used for updating an existing user's profile. Null fields are ignored during the update process.")
public class RegisteredUserUpdateDTO {

  /**
   * The new email address.
   */
  @Schema(description = "The new email address. Must be unique within the system.", example = "new_email@example.com")
  private String email;

  /**
   * The new username.
   */
  @Schema(description = "The new username. Must be unique within the system.", example = "cool_scholar_99")
  private String username;

  /**
   * The new password.
   */
  @Schema(description = "The new password. If provided, it will be encrypted and stored securely.", example = "NewSecurePass!23")
  private String password;

  /**
   * The updated full name.
   */
  @Schema(description = "The updated full legal name or display name.", example = "Luigi Verdi")
  private String fullName;

  /**
   * The updated list of bookmarks.
   */
  @Schema(description = "The updated list of bookmarked papers. Usually managed via dedicated endpoints.")
  private List<BookmarkedPaperDTO> bookmarkedPapers;
}