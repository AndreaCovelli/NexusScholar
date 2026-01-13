package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Lightweight DTO representing a summary of a publication.
 *
 * <p>Used when a full paper object is not required, for example in lists or brief citations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicationSummary {

  @Field("paper_id")
  private String paperId;

  private Integer year;
  private String title;
}
