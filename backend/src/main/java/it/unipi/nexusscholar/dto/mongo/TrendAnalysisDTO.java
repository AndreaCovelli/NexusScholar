package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for analyzing publication trends within specific fields of study.
 * <p>
 * This class is used to transport aggregated data indicating the volume of
 * publications for a specific topic over a specific year.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendAnalysisDTO {

  /**
   * The specific field of study being analyzed (e.g., "Artificial Intelligence").
   */
  private String fieldOfStudy;

  /**
   * The reference year for the trend analysis.
   */
  private Integer year;

  /**
   * The number of papers created/published in the specified field during the specified year.
   */
  private Integer paperCreated;
}