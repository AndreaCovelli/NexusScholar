package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents an entry in the paper leaderboard.
 *
 * <p>This class is used to map the results of aggregation queries that calculate the popularity of
 * papers based on bookmarks.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperLeaderboard {

  @Field("paper_id")
  private String paperId;

  private String title;

  /** The total number of bookmarks this paper has received in the queried period. */
  @Field("bookmarked_received")
  private int bookmarkedReceived;
}
