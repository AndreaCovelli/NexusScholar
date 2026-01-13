package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Data Transfer Object for analyzing publication trends within specific fields of study.")
public class TrendAnalysisDTO {

  /**
   * The specific field of study being analyzed (e.g., "Artificial Intelligence").
   */
  @Schema(description = "The specific field of study being analyzed.", example = "Artificial Intelligence")
  private String fieldOfStudy;

  /**
   * The reference year for the trend analysis.
   */
  @Schema(description = "The reference year for the trend analysis.", example = "2023")
  private Integer year;

  /**
   * The number of papers created/published in the specified field during the specified year.
   */
  @Schema(description = "The number of papers created/published in the specified field during the specified year.", example = "150")
  private Integer paperCreated;
}