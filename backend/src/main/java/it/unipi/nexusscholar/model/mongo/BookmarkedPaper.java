package it.unipi.nexusscholar.model.mongo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a snapshot of a scientific paper bookmarked by a user.
 * <p>
 * This class is designed to be used as an embedded document within {@link RegisteredUser}.
 * It stores a lightweight summary of the paper (ID, title, and save date) to facilitate
 * efficient retrieval and display of a user's bookmarks without querying the main
 * papers collection immediately.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedPaper {

  /**
   * The unique identifier of the referenced paper.
   * <p>
   * This ID corresponds to the primary key in the main papers collection.
   * </p>
   */
  @Field("paper_id")
  private String paperId;

  /**
   * The title of the paper.
   * <p>
   * This is stored redundantly here to allow for the immediate display of the
   * bookmark title in the user's profile.
   * </p>
   */
  private String title;

  /**
   * The exact date and time when the user added this paper to their bookmarks.
   */
  @Field("saved_at")
  private LocalDateTime savedAt;
}