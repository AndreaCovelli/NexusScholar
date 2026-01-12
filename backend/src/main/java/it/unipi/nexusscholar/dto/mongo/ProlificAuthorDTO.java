package it.unipi.nexusscholar.dto.mongo;

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
public class ProlificAuthorDTO {

  /**
   * The unique identifier of the author.
   */
  private String authorId;

  /**
   * The name of the author.
   */
  private String authorName;
}