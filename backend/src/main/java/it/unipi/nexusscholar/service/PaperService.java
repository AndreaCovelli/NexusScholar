package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import java.util.List;

/**
 * Service interface for managing Paper entities. Handles CRUD operations and maintains
 * bidirectional consistency with Authors.
 */
public interface PaperService {

  /**
   * Saves or updates a Paper.
   *
   * <p>Business Logic: - If ID is null: Creates a new Paper (Insert). - If ID is present: Updates
   * the existing Paper (Update).
   *
   * <p>Integrity Checks & Side Effects: - All authors referenced must already exist in the
   * database. - Automatically updates totalPublications and publicationSummary for linked authors.
   * - DOI uniqueness is enforced (if provided).
   *
   * @throws BusinessException if validation fails
   */
  PaperDTO savePaper(PaperDTO paperDTO);

  /**
   * Retrieves a paper by its MongoDB ID.
   *
   * @throws BusinessException if paper not found
   */
  PaperDTO getPaperById(String id);

  /** Searches papers by title (case-insensitive partial match). */
  List<PaperDTO> searchPapersByTitle(String title);

  /** Retrieves papers published in a specific year. */
  List<PaperDTO> getPapersByYear(Integer year);

  /**
   * Deletes a paper by ID. Side Effects: Removes paper from all associated authors' publication
   * histories.
   */
  void deletePaper(String id);
}
