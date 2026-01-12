package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO representing the result of a Venue Impact analysis.
 * <p>
 * Shows the publication volume of a specific venue (Conference or Journal)
 * for a given year.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueAnalysis {

  /**
   * The name of the publication venue.
   */
  private String venue;

  /**
   * The year of analysis.
   */
  private Integer year;

  /**
   * The number of papers published in this venue during this year.
   */
  @Field("paper_created")
  private Integer paperCreated;
}