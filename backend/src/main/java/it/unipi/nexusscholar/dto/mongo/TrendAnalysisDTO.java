package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for trend analysis results. Represents paper counts per field of study per year. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendAnalysisDTO {
  private String fieldOfStudy;
  private Integer year;
  private Integer paperCreated;
}
