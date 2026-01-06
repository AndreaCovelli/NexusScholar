package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Author;
import java.util.List;
import java.util.Optional;
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

  /** Case-insensitive partial name search. Enables author discovery by name fragment. */
  List<Author> findByNameContainingIgnoreCase(String name);
}
