package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for prolific author identification results. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProlificAuthorDTO {
  private String authorId;
  private String authorName;
}
