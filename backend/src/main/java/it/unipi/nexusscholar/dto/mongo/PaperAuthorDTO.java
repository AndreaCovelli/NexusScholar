package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an author associated with a specific paper.
 * <p>
 * This contains a subset of author information typically embedded within a {@link PaperDTO}.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperAuthorDTO {

  /**
   * The unique identifier of the author.
   */
  private String id;

  /**
   * The full name of the author.
   */
  private String name;
}