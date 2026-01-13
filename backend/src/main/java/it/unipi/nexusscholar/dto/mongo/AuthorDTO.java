package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for the Author entity.
 *
 * <p>This class is used to transfer author details to the client, including personal information
 * and a summary of their publication history.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description =
        "Data Transfer Object (DTO) for the Author entity. Contains personal info and publication history.")
public class AuthorDTO {

  /** The unique identifier of the author. */
  @Schema(
      description = "The unique identifier of the author.",
      example = "507f1f77bcf86cd799439011")
  private String id;

  /** The full name of the author. */
  @Schema(description = "The full name of the author.", example = "Alan Turing")
  private String name;

  /** The external identifier assigned by Semantic Scholar. */
  @Schema(
      description = "The external identifier assigned by Semantic Scholar.",
      example = "1741101")
  private String s2AuthorId;

  /** The total count of publications associated with this author. */
  @Schema(
      description = "The total count of publications associated with this author.",
      example = "42")
  private Integer totalPublications;

  /** A list of summary objects for the papers written by this author. */
  @Schema(description = "A list of summary objects for the papers written by this author.")
  private List<PublicationSummaryDTO> publicationsSummary;
}
