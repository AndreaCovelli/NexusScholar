package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the evolution of scientific collaboration over time.
 * <p>
 * This is typically used to graph the average team size (number of authors) per paper per year.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationEvolutionDTO {

  /**
   * The average number of authors per paper for the specified year.
   */
  private double avgAuthors;

  /**
   * The reference year for the collaboration statistic.
   */
  private int year;
}