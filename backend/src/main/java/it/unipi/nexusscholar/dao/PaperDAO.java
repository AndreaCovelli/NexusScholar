package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.model.mongo.Paper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaperDAO extends MongoRepository<Paper, String> {

    /**
     * Finds papers containing the specified string in the title (case insensitive).
     *
     * @param title Title fragment.
     * @return List of matching papers.
     */
    List<Paper> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds a paper by its unique DOI.
     * Useful for checking duplicates before insertion.
     *
     * @param doi Digital Object Identifier.
     * @return Optional containing the paper if found.
     */
    Optional<Paper> findByDoi(String doi);

    /**
     * Finds papers published in a specific year.
     *
     * @param year The publication year.
     * @return List of papers.
     */
    List<Paper> findByYear(Integer year);
}