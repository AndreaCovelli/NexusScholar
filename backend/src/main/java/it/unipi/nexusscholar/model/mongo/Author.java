package it.unipi.nexusscholar.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document representing an academic Author.
 * <p>
 * This class maps to the {@code authors} collection in the database. It stores
 * the author's personal details, external identifiers, and a summary of their
 * publications to optimize read operations (denormalization).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "authors")
public class Author {

  /**
   * The unique identifier for the author (MongoDB ObjectId).
   */
  @Id
  private String id;

  /**
   * The full name of the author.
   */
  private String name;

  /**
   * The external identifier provided by Semantic Scholar (S2).
   * <p>
   * Used for data ingestion, deduplication, and linking to external datasets.
   * </p>
   */
  @Field("s2_author_id")
  private String s2AuthorId;

  /**
   * The total number of publications associated with this author.
   * <p>
   * This is a pre-calculated counter to allow efficient sorting and filtering
   * without aggregating the entire {@code publications_summary} list.
   * </p>
   */
  @Field("total_publications")
  private Integer totalPublications;

  /**
   * A denormalized list of publication summaries.
   * <p>
   * Contains a subset of data (e.g., title, year, venue) for the papers
   * written by this author, avoiding the need for complex joins with the Papers collection.
   * </p>
   */
  @Field("publications_summary")
  private List<PublicationSummary> publicationsSummary;
}