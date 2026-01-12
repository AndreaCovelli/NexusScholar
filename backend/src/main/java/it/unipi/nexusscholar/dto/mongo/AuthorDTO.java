package it.unipi.nexusscholar.dto.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for the Author entity.
 * <p>
 * This class is used to transfer author details to the client, including
 * personal information and a summary of their publication history.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorDTO {

  /**
   * The unique identifier of the author.
   */
  private String id;

  /**
   * The full name of the author.
   */
  private String name;

  /**
   * The external identifier assigned by Semantic Scholar.
   */
  private String s2AuthorId;

  /**
   * The total count of publications associated with this author.
   */
  private Integer totalPublications;

  /**
   * A list of summary objects for the papers written by this author.
   */
  private List<PublicationSummaryDTO> publicationsSummary;
}