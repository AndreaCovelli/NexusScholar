package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperLeaderboard {
  @Field("paper_id")
  private String paperId;

  private String title;

  @Field("bookmarked_received")
  private int bookmarkedReceived;
}
