package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import java.util.List;

/**
 * Service interface for author-related analytical operations.
 * <p>
 * This interface defines the business logic for extracting insights about authors,
 * such as identifying prolific researchers based on publication metrics.
 * </p>
 */
public interface AuthorAnalysisService {

  /**
   * Identifies authors who have published more than the specified minimum number of papers in a year.
   * <p>
   * This method triggers an aggregation in the persistence layer to filter authors
   * based on their publication volume.
   * </p>
   *
   * @param minPublications The minimum number of publications required to be considered "prolific".
   * @return A list of {@link ProlificAuthorDTO} containing the names and IDs of the matching authors.
   */
  List<ProlificAuthorDTO> getProlificAuthors(int minPublications);
}