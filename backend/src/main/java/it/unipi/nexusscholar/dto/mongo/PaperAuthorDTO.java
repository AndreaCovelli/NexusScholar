package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Data Transfer Object representing an author associated with a specific paper.")
public class PaperAuthorDTO {

  /**
   * The unique identifier of the author.
   */
  @Schema(description = "The unique identifier of the author.", example = "507f1f77bcf86cd799439011")
  private String id;

  /**
   * The full name of the author.
   */
  @Schema(description = "The full name of the author.", example = "Grace Hopper")
  private String name;
}