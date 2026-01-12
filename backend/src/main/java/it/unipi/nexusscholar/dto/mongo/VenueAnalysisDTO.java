package it.unipi.nexusscholar.dto.mongo;

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
public class VenueAnalysisDTO {

  /**
   * The name of the venue (e.g., "ICSE", "Nature", "IEEE Transactions").
   */
  private String venue;

  /**
   * The reference year for the analysis.
   */
  private Integer year;

  /**
   * The number of papers published in this venue during the specified year.
   */
  private Integer paperCreated;
}