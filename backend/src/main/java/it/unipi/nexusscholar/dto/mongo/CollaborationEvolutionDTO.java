package it.unipi.nexusscholar.dto.mongo;

import it.unipi.nexusscholar.model.mongo.Permission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
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
