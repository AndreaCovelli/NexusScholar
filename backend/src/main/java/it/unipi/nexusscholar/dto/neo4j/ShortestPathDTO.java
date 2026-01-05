package it.unipi.nexusscholar.dto.neo4j;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortestPathDTO {
  private List<PathNodeDTO> nodes;
  private List<PathRelationshipDTO> relationships;
  private int degreeSeparation;
}
