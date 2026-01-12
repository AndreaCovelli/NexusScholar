package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Paper;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for managing {@link Paper} documents.
 * <p>
 * Provides standard CRUD operations and custom finder methods with pagination support.
 * </p>
 */
@Repository
public interface PaperDAO extends MongoRepository<Paper, String> {

  /**
   * Finds a paper by its Digital Object Identifier (DOI).
   *
   * @param doi The DOI string.
   * @return An {@link Optional} containing the paper if found.
   */
  Optional<Paper> findByDoi(String doi);

  /**
   * Searches for papers whose title contains the specified string (case-insensitive).
   *
   * @param title    The keyword to search for in the title.
   * @param pageable Pagination information.
   * @return A page of matching papers.
   */
  Page<Paper> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  /**
   * Retrieves all papers published in a specific year.
   *
   * @param year     The publication year.
   * @param pageable Pagination information.
   * @return A page of papers published in that year.
   */
  Page<Paper> findByYear(Integer year, Pageable pageable);

  /**
   * Performs a full-text search on the papers collection.
   * <p>
   * This method relies on a MongoDB Text Index being created on the relevant fields
   * (e.g., title, abstract).
   * </p>
   *
   * @param keyword  The text to search for.
   * @param pageable Pagination information.
   * @return A page of papers matching the text query.
   */
  @Query("{'$text': {'$search': ?0}}")
  Page<Paper> findByTextSearch(String keyword, Pageable pageable);
}