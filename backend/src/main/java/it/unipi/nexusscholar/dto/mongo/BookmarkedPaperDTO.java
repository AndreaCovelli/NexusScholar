package it.unipi.nexusscholar.dto.mongo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO representing a paper saved in a user's bookmark list.
 * <p>
 * Contains minimal information required to display the bookmark on the client side
 * without fetching the full paper document.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedPaperDTO {

  /**
   * The unique identifier of the referenced paper.
   */
  private String paperId;

  /**
   * The title of the paper.
   */
  private String title;

  /**
   * The timestamp when the user added this bookmark.
   */
  private LocalDateTime savedAt;
}