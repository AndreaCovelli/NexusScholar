package it.unipi.nexusscholar.dto.mongo;

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
public class PaperLeaderboardDTO {

  /**
   * The ID of the popular paper.
   */
  private String paperId;

  /**
   * The title of the paper.
   */
  private String title;

  /**
   * The total number of bookmarks this paper received in the queried period.
   */
  private int bookmarkedReceived;
}