package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO representing a paper saved in a user's bookmark list.
 *
 * <p>Contains minimal information required to display the bookmark on the client side without
 * fetching the full paper document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight DTO representing a paper saved in a user's bookmark list.")
public class BookmarkedPaperDTO {

  /** The unique identifier of the referenced paper. */
  @Schema(
      description = "The unique identifier of the referenced paper.",
      example = "64b2c3d4e5f6g7h8i9j0k1")
  private String paperId;

  /** The title of the paper. */
  @Schema(description = "The title of the paper.", example = "Attention Is All You Need")
  private String title;

  /** The timestamp when the user added this bookmark. */
  @Schema(
      description = "The timestamp when the user added this bookmark.",
      example = "2023-10-27T10:15:30")
  private LocalDateTime savedAt;
}
