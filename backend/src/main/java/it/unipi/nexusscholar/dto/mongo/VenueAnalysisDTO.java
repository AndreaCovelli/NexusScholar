package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for analyzing publication volume across different venues.
 * <p>
 * This class transports data regarding how many papers were published
 * in a specific venue (conference or journal) during a specific year.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for analyzing publication volume across different venues.")
public class VenueAnalysisDTO {

  /**
   * The name of the venue (e.g., "ICSE", "Nature", "IEEE Transactions").
   */
  @Schema(description = "The name of the venue (e.g., conferences or journals).", example = "ICSE")
  private String venue;

  /**
   * The reference year for the analysis.
   */
  @Schema(description = "The reference year for the analysis.", example = "2022")
  private Integer year;

  /**
   * The number of papers published in this venue during the specified year.
   */
  @Schema(description = "The number of papers published in this venue during the specified year.", example = "45")
  private Integer paperCreated;
}