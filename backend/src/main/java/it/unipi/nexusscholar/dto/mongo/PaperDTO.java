package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object for Paper entities. Used for API request/response serialization. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperDTO {
  private String id;
  private String title;
  private Integer year;
  private String dblpKey;
  private String doi;
  private String abstractText;
  private List<String> fieldsOfStudy;

  // CRITICAL CHANGE: Use embedded author objects instead of plain strings
  // This maintains consistency with the Paper model and enables proper author linking
  private List<PaperAuthorDTO> authors;

  private List<String> venue;
}
