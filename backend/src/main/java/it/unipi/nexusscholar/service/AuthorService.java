package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import java.util.List;

/**
 * Service interface for managing Author entities. Handles CRUD operations and maintains data
 * consistency with Paper collection.
 */
public interface AuthorService {

  /**
   * Saves or updates an Author.
   *
   * <p>Business Logic: - Insert (ID is null): Creates a new Author. S2 Author ID must be unique.
   * New authors cannot be created with a publication history. - Update (ID exists): Updates author
   * details and publication list.
   *
   * <p>Side Effects (Update only): - Validates that all Paper IDs in publication summary exist. -
   * If a paper is added to history, this author is added to that Paper's author list. -
   * Automatically recalculates totalPublications count.
   *
   * @throws BusinessException if validation fails
   */
  AuthorDTO saveAuthor(AuthorDTO authorDTO);

  /**
   * Retrieves an author by their Semantic Scholar ID.
   *
   * @throws BusinessException if author not found
   */
  AuthorDTO getAuthorByS2Id(String s2AuthorId);

  /** Searches for authors by name (case-insensitive partial match). */
  List<AuthorDTO> searchAuthorsByName(String name);

  /** Finds authors with more than the specified number of publications. */
  List<AuthorDTO> getAuthorsWithMinPublications(Integer minPublications);

  /**
   * Deletes an author by their Semantic Scholar ID.
   *
   * @throws BusinessException if author not found
   */
  void deleteAuthorByS2Id(String s2AuthorId);
}
