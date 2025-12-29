package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorDTO {
  private String id;
  private String name;
  private String s2AuthorId;
  private Integer totalPublications;
  private List<PublicationSummaryDTO> publicationsSummary;
}
