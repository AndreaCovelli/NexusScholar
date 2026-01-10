package it.unipi.nexusscholar.model.mongo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedPaper {

  @Field("paper_id")
  private String paperId;

  private String title;

  @Field("saved_at")
  private LocalDateTime savedAt;
}
