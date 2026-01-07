package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Author entities.
 * Handles CRUD operations and maintains data consistency with the Paper collection.
 */
public interface AuthorService {

    /**
     * Saves or updates an Author.
     *
     * <p><b>Business Logic:</b>
     * <ul>
     * <li><b>Insert (ID is null):</b> Creates a new Author. S2 Author ID must be unique. New authors cannot be created with a publication history.</li>
     * <li><b>Update (ID exists):</b> Updates author details and publication list.</li>
     * </ul>
     *
     * <p><b>Side Effects (Update only):</b>
     * <ul>
     * <li>Validates that all Paper IDs in publication summary exist.</li>
     * <li>If a paper is added to history, this author is added to that Paper's author list.</li>
     * <li>If a paper is removed from history, the author is removed from that Paper.</li>
     * <li>If a paper remains with 0 authors, it is deleted.</li>
     * <li>Automatically recalculates totalPublications count.</li>
     * </ul>
     *
     * @param authorDTO The author data transfer object.
     * @return The saved AuthorDTO.
     * @throws it.unipi.nexusscholar.exception.BusinessException if validation fails.
     */
    AuthorDTO saveAuthor(AuthorDTO authorDTO);

    /**
     * Retrieves an author by their Semantic Scholar ID.
     *
     * @param s2AuthorId The Semantic Scholar ID.
     * @return The requested AuthorDTO.
     * @throws it.unipi.nexusscholar.exception.BusinessException if author is not found.
     */
    AuthorDTO getAuthorByS2Id(String s2AuthorId);

    /**
     * Searches for authors by name with pagination.
     *
     * @param name     The name (or partial name) to search for.
     * @param pageable The pagination information.
     * @return A page of authors matching the criteria.
     */
    Page<AuthorDTO> searchAuthorsByName(String name, Pageable pageable);

    /**
     * Finds authors with a minimum number of publications with pagination.
     *
     * @param minPublications The minimum number of publications required.
     * @param pageable        The pagination information.
     * @return A page of authors matching the criteria.
     */
    Page<AuthorDTO> getAuthorsWithMinPublications(Integer minPublications, Pageable pageable);

    /**
     * Deletes an author by their internal MongoDB ID.
     *
     * <p><b>Side Effects:</b>
     * <ul>
     * <li>Removes the author reference from all associated papers.</li>
     * <li>If a paper ends up with no authors, the paper is deleted.</li>
     * </ul>
     *
     * @param id The internal MongoDB ID of the author.
     * @throws it.unipi.nexusscholar.exception.BusinessException if author is not found.
     */
    void deleteAuthorById(String id);
}