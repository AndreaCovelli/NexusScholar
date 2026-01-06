package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for collaboration evolution analysis results. Tracks average author count per paper over
 * time.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationEvolutionDTO {
  private double avgAuthors;
  private int year;
}
