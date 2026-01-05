package it.unipi.nexusscholar.dto.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathNodeDTO {
  private String elementId;
  private String type; // "Author" or "Paper"
  private String displayName; // name for Author, title for Paper
}
