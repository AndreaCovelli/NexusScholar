package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Author;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link Author} documents.
 *
 * <p>Provides standard CRUD operations and custom query methods for filtering authors by name,
 * external IDs, and publication metrics.
 */
@Repository
public interface AuthorDAO extends MongoRepository<Author, String> {

  /**
   * Finds an author by their unique Semantic Scholar ID.
   *
   * <p>Primarily used for ensuring data consistency during import processes and avoiding duplicate
   * entries for the same researcher.
   *
   * @param s2AuthorId The Semantic Scholar ID string.
   * @return An {@link Optional} containing the author if found, or empty otherwise.
   */
  Optional<Author> findByS2AuthorId(String s2AuthorId);

  /**
   * Performs a prefix-based case-sensitive search on the author's name.
   *
   * @param name The prefix string to search for (e.g., "Smi" for "Smith").
   * @param pageable Pagination information (page number, size, and sorting).
   * @return A {@link Page} of authors whose names start with the given string.
   */
  Page<Author> findByNameStartsWith(String name, Pageable pageable);

  /**
   * Retrieves a paginated list of authors who have published more than a specific number of papers.
   *
   * <p>This method uses the pre-calculated {@code total_publications} field for efficient
   * filtering.
   *
   * @param totalPublications The exclusive lower bound for the publication count.
   * @param pageable Pagination information.
   * @return A {@link Page} of authors meeting the criteria.
   */
  Page<Author> findByTotalPublicationsGreaterThan(Integer totalPublications, Pageable pageable);
}
