package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Paper;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for Paper documents. Provides CRUD operations with pagination
 * support for large result sets.
 */
@Repository
public interface PaperDAO extends MongoRepository<Paper, String> {

  /** Finds a paper by its DOI. DOIs are globally unique identifiers, enabling deduplication. */
  Optional<Paper> findByDoi(String doi);

  /** Searches papers by title with pagination. Case-insensitive partial match. */
  Page<Paper> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  /** Retrieves papers by publication year with pagination. */
  Page<Paper> findByYear(Integer year, Pageable pageable);

  /**
   * Performs a full-text search using MongoDB's $text operator.
   */
  @Query("{'$text': {'$search': ?0}}")
  Page<Paper> findByTextSearch(String keyword, Pageable pageable);
}