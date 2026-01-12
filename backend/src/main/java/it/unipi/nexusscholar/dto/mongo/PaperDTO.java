package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a scientific paper stored in MongoDB.
 * <p>
 * This class encapsulates all relevant metadata regarding a research paper,
 * including its identifiers, bibliographic info, authors, and classification.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperDTO {

  /**
   * The unique identifier for the paper (usually the MongoDB ObjectId).
   */
  private String id;

  /**
   * The full title of the paper.
   */
  private String title;

  /**
   * The year the paper was published.
   */
  private Integer year;

  /**
   * The unique key identifying the paper in the DBLP computer science bibliography.
   */
  private String dblpKey;

  /**
   * The Digital Object Identifier (DOI) of the paper.
   */
  private String doi;

  /**
   * The abstract or summary text of the paper.
   */
  private String abstractText;

  /**
   * A list of fields of study or topics associated with this paper
   * (e.g., "Machine Learning", "Database Systems").
   */
  private List<String> fieldsOfStudy;

  /**
   * The list of authors who contributed to this paper.
   */
  private List<PaperAuthorDTO> authors;

  /**
   * The venue(s) where the paper was presented or published
   * (e.g., conference names, journal titles).
   */
  private List<String> venue;
}