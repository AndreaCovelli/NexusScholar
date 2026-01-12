package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO representing the evolution of scientific collaboration.
 * <p>
 * specific metrics regarding the average number of authors per paper
 * for a given year.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationEvolution {

  /**
   * The average number of authors per paper in the specified year.
   */
  @Field("avg_authors")
  private double avgAuthors;

  /**
   * The year of analysis.
   */
  private int year;
}