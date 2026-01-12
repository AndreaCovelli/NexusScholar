package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO representing a fully registered user sent in API responses.
 * <p>
 * Extends {@link UserDTO} to include specific fields for registered members,
 * such as their full name and their collection of bookmarked papers.
 * </p>
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredUserDTO extends UserDTO {

  /**
   * The user's full name.
   */
  private String fullName;

  /**
   * The list of papers saved by the user.
   */
  private List<BookmarkedPaperDTO> bookmarkedPapers;
}