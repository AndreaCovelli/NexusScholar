package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a summary of a publication.
 * <p>
 * This class is designed to provide a lightweight view of a publication,
 * typically used in lists, search results, or API responses where the
 * full details of the paper are not required.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Summary of a publication, typically used in lists.")
public class PublicationSummaryDTO {

  /**
   * The unique identifier of the paper.
   */
  @Schema(description = "The unique identifier of the paper.", example = "609c1234567890abcdef1234")
  private String paperId;

  /**
   * The year the paper was published.
   */
  @Schema(description = "The year of publication.", example = "2023")
  private Integer year;

  /**
   * The official title of the paper.
   */
  @Schema(description = "The title of the paper.", example = "Advances in Neural Networks")
  private String title;
}