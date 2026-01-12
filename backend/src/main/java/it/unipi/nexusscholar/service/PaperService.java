package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   */
  PaperDTO savePaper(PaperDTO paperDTO);

  /** Retrieves a paper by its MongoDB ID. */
  PaperDTO getPaperById(String id);

  /** * Searches papers by title (case-insensitive partial match). Supports pagination. */
  Page<PaperDTO> searchPapersByTitle(String title, Pageable pageable);

  /** * Retrieves papers published in a specific year. Supports pagination. */
  Page<PaperDTO> getPapersByYear(Integer year, Pageable pageable);

  /**
   * Deletes a paper by ID. Side Effects: Removes paper from all associated authors' publication
   * histories.
   */
  void deletePaper(String id);

  /**
   * Searches papers using MongoDB full-text search. This allows searching for keywords across all
   * text-indexed fields (e.g., title, abstract). Supports pagination.
   */
  Page<PaperDTO> searchPapersByText(String keyword, Pageable pageable);
}
