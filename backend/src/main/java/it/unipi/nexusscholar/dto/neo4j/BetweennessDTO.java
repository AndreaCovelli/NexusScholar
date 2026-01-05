package it.unipi.nexusscholar.dto.neo4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetweennessDTO {
  private String paperTitle;
  private double betweenness;
}
