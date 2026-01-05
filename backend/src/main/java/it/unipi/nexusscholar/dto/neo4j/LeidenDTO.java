package it.unipi.nexusscholar.dto.neo4j;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeidenDTO {
  private int communityId;
  private List<String> authors;
}
