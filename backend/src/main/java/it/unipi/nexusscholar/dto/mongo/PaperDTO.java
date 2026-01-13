package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a scientific paper stored in MongoDB.
 *
 * <p>This class encapsulates all relevant metadata regarding a research paper, including its
 * identifiers, bibliographic info, authors, and classification.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Data Transfer Object (DTO) representing a scientific paper stored in MongoDB.")
public class PaperDTO {

  /** The unique identifier for the paper (usually the MongoDB ObjectId). */
  @Schema(
      description = "The unique identifier for the paper (usually the MongoDB ObjectId).",
      example = "609c1234567890abcdef1234")
  private String id;

  /** The full title of the paper. */
  @Schema(description = "The full title of the paper.", example = "Attention Is All You Need")
  private String title;

  /** The year the paper was published. */
  @Schema(description = "The year the paper was published.", example = "2017")
  private Integer year;

  /** The unique key identifying the paper in the DBLP computer science bibliography. */
  @Schema(
      description =
          "The unique key identifying the paper in the DBLP computer science bibliography.",
      example = "conf/nips/VaswaniSPUJGKP17")
  private String dblpKey;

  /** The Digital Object Identifier (DOI) of the paper. */
  @Schema(
      description = "The Digital Object Identifier (DOI) of the paper.",
      example = "10.1145/3456789.1234567")
  private String doi;

  /** The abstract or summary text of the paper. */
  @Schema(
      description = "The abstract or summary text of the paper.",
      example =
          "The dominant sequence transduction models are based on complex recurrent or convolutional neural networks...")
  private String abstractText;

  /**
   * A list of fields of study or topics associated with this paper (e.g., "Machine Learning",
   * "Database Systems").
   */
  @Schema(
      description = "A list of fields of study or topics associated with this paper.",
      example = "[\"Machine Learning\", \"Natural Language Processing\"]")
  private List<String> fieldsOfStudy;

  /** The list of authors who contributed to this paper. */
  @Schema(description = "The list of authors who contributed to this paper.")
  private List<PaperAuthorDTO> authors;

  /**
   * The venue(s) where the paper was presented or published (e.g., conference names, journal
   * titles).
   */
  @Schema(
      description = "The venue(s) where the paper was presented or published.",
      example = "[\"NeurIPS\", \"arXiv\"]")
  private List<String> venue;
}
