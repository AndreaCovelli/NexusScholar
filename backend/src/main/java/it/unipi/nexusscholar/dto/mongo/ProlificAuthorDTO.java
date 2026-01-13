package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the result of a "Prolific Author" analysis.
 * <p>
 * This lightweight class is used to return a list of high-output researchers
 * without including their full publication history.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object representing the result of a 'Prolific Author' analysis (high-output researchers).")
public class ProlificAuthorDTO {

  /**
   * The unique identifier of the author.
   */
  @Schema(description = "The unique identifier of the author.", example = "507f1f77bcf86cd799439011")
  private String authorId;

  /**
   * The name of the author.
   */
  @Schema(description = "The name of the author.", example = "Yoshua Bengio")
  private String authorName;
}