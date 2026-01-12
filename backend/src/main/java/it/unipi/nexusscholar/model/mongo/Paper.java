package it.unipi.nexusscholar.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document representing an academic paper.
 * <p>
 * This is the core entity of the application, stored in the "papers" collection.
 * It contains bibliographic details, metadata about authors and venues, and
 * classification fields (fields of study).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "papers")
public class Paper {

  /**
   * The unique identifier of the paper.
   */
  @Id
  private String id;

  /**
   * The title of the publication.
   */
  private String title;

  /**
   * The year of publication.
   */
  private Integer year;

  /**
   * The DBLP key, serving as an external reference identifier.
   */
  @Field("dblp_key")
  private String dblpKey;

  /**
   * The Digital Object Identifier (DOI) of the paper.
   * <p>
   * Used for unique identification and deduplication across scientific databases.
   * </p>
   */
  private String doi;

  /**
   * The abstract or summary of the paper's content.
   */
  @Field("abstract")
  private String abstractText;

  /**
   * A list of academic fields or topics associated with this paper (e.g., "Machine Learning", "Physics").
   */
  @Field("fields_of_study")
  private List<String> fieldsOfStudy;

  /**
   * The list of authors who contributed to this paper.
   */
  private List<PaperAuthor> authors;

  /**
   * The venue(s) where the paper was published (e.g., Journal name, Conference name).
   * <p>
   * Stored as a list to handle cases where venue data might be ambiguous or multiple.
   * </p>
   */
  private List<String> venue;
}