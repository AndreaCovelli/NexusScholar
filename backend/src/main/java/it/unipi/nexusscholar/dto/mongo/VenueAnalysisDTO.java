package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for venue impact analysis results. Ranks venues by publication count per year. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueAnalysisDTO {
  private String venue;
  private Integer year;
  private Integer paperCreated;
}
