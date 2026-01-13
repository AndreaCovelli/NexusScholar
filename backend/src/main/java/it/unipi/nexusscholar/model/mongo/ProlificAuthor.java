package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Data Transfer Object (DTO) representing the result of a "Prolific Author" analysis.
 *
 * <p>This class is not a direct mapping of a MongoDB collection but a projection result returned by
 * aggregation queries (e.g., identifying authors with high publication counts).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProlificAuthor {

  /** The unique identifier of the author. */
  @Field("author_id")
  private String authorId;

  /** The name of the author. */
  @Field("author_name")
  private String authorName;
}
