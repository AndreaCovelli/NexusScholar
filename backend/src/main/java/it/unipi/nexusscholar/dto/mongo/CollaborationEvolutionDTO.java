package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the evolution of scientific collaboration over time.
 *
 * <p>This is typically used to graph the average team size (number of authors) per paper per year.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description =
        "Data Transfer Object representing the evolution of scientific collaboration over time (avg authors per year).")
public class CollaborationEvolutionDTO {

  /** The average number of authors per paper for the specified year. */
  @Schema(
      description = "The average number of authors per paper for the specified year.",
      example = "3.5")
  private double avgAuthors;

  /** The reference year for the collaboration statistic. */
  @Schema(description = "The reference year for the collaboration statistic.", example = "2023")
  private int year;
}
