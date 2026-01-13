package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO representing the result of a "Hot Topics" trend analysis.
 *
 * <p>Aggregates data to show the volume of papers published in a specific field of study during a
 * specific year.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendAnalysis {

  /** The academic field of study (e.g., "Artificial Intelligence"). */
  @Field("field_of_study")
  private String fieldOfStudy;

  /** The year of analysis. */
  private Integer year;

  /** The total number of papers published in this field for this year. */
  @Field("paper_created")
  private Integer paperCreated;
}
