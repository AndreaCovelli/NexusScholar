package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an entry in the paper popularity leaderboard.
 * <p>
 * Used to transfer aggregation results, showing which papers have received
 * the most bookmarks within a specific time frame.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing an entry in the paper popularity leaderboard.")
public class PaperLeaderboardDTO {

  /**
   * The ID of the popular paper.
   */
  @Schema(description = "The ID of the popular paper.", example = "609c1234567890abcdef1234")
  private String paperId;

  /**
   * The title of the paper.
   */
  @Schema(description = "The title of the paper.", example = "Deep Residual Learning for Image Recognition")
  private String title;

  /**
   * The total number of bookmarks this paper received in the queried period.
   */
  @Schema(description = "The total number of bookmarks this paper received in the queried period.", example = "150")
  private int bookmarkedReceived;
}