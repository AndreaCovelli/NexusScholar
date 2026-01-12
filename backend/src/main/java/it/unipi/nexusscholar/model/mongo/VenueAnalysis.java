package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueAnalysis {
  private String venue;
  private Integer year;

  @Field("paper_created")
  private Integer paperCreated;
}
