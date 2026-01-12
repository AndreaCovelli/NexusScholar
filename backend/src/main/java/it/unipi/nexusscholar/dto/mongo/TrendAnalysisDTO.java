package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendAnalysisDTO {
  private String fieldOfStudy;
  private Integer year;
  private Integer paperCreated;
}
