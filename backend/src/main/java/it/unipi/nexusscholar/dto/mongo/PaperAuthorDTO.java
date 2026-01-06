package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for author references embedded within PaperDTO. Provides the minimum necessary author
 * information for paper displays.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperAuthorDTO {
  private String id;
  private String name;
}
