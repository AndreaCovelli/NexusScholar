package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.model.mongo.Author;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AuthorDAO extends MongoRepository<Author, String> {

    /**
     * Finds an author using their external Semantic Scholar ID.
     *
     * @param s2AuthorId The unique identifier from Semantic Scholar.
     * @return The author desired, if possible.
     */
    Optional<Author> findByS2AuthorId(String s2AuthorId);

    /**
     * Finds all authors whose name contains the specified string.
     *
     * @param name The part of the name to search for.
     * @return A list of authors matching the search criteria.
     */
    List<Author> findByNameContainingIgnoreCase(String name);

    /**
     * Executes a native MongoDB query to find authors with a number of publications
     * greater than the specified value.
     *
     * @param minPublications The minimum number of publications required.
     * @return A list of authors satisfying the requirement.
     */
    @Query("{ 'total_publications' : { $gt: ?0 } }")
    List<Author> findAuthorsWithMoreThan(Integer minPublications);

    /**
     * Deletes an author based on their external Semantic Scholar ID.
     *
     * @param s2AuthorId The external ID of the author to remove.
     */
    void deleteByS2AuthorId(String s2AuthorId);
}