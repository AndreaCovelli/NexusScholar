package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Result object for the Collaboration Evolution aggregation pipeline. Tracks average number of
 * authors per paper over time.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationEvolution {
  @Field("avg_authors")
  private double avgAuthors;

  private int year;
}
