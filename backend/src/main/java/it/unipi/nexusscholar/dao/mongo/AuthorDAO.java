package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Author;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for Author documents. Provides standard CRUD operations and custom
 * query methods.
 */
@Repository
public interface AuthorDAO extends MongoRepository<Author, String> {

  /**
   * Finds an author by their Semantic Scholar ID. Used for deduplication and external system
   * integration.
   */
  Optional<Author> findByS2AuthorId(String s2AuthorId);

  /** Prefix-based name search. Case-sensitive. */
  Page<Author> findByNameStartsWith(String name, Pageable pageable);

  /**
   * Retrieves authors with a total publication count strictly greater than the specified value.
   * Useful for identifying prolific authors or filtering based on productivity.
   */
  Page<Author> findByTotalPublicationsGreaterThan(Integer totalPublications, Pageable pageable);
}
