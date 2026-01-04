package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.model.mongo.Author;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorDAO extends MongoRepository<Author, String>, AuthorDAOCustom {

  /**
   * Show the history of papers published by an author. value = "{ '_id': ?0 }" is the filter of the
   * query fields = "{ 'publications_summary': 1, '_id': 0 }" are the projections
   *
   * @param authorId Id of the desired author
   * @return Document with the list of publication of the desired author
   */
  @Query(value = "{ '_id': ?0 }", fields = "{ 'publications_summary': 1, '_id': 0 }")
  Optional<Document> findPublicationsHistory(String authorId);
}
