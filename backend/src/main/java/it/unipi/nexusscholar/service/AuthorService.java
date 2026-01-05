package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.AuthorDTO;

import java.util.List;

public interface AuthorService {

    /**
     * Saves or updates an Author.
     * Converts the input DTO to an entity, saves it, and returns the resulting DTO.
     *
     * @param authorDTO The author data transfer object to save.
     * @return The saved author as a DTO.
     */
    AuthorDTO saveAuthor(AuthorDTO authorDTO);

    /**
     * Retrieves an author by their external Semantic Scholar ID.
     *
     * @param s2AuthorId The external ID from Semantic Scholar.
     * @return The found AuthorDTO.
     */
    AuthorDTO getAuthorByS2Id(String s2AuthorId);

    /**
     * Searches for authors whose name contains the specified string.
     *
     * @param name The partial name to search for.
     * @return A list of AuthorDTO objects.
     */
    List<AuthorDTO> searchAuthorsByName(String name);

    /**
     * Finds authors who have more than a specific number of publications.
     *
     * @param minPublications The minimum threshold of publications.
     * @return A list of AuthorDTO objects meeting the criteria.
     */
    List<AuthorDTO> getAuthorsWithMinPublications(Integer minPublications);

    /**
     * Deletes an author based on their Semantic Scholar ID.
     *
     * @param s2AuthorId The external ID of the author to delete.
     */
    void deleteAuthorByS2Id(String s2AuthorId);
}

