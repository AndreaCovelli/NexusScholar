package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Result object for the Venue Impact Analysis aggregation pipeline. Ranks venues by paper count per
 * year.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueAnalysis {
  private String venue;
  private Integer year;

  @Field("paper_created")
  private Integer paperCreated;
}
