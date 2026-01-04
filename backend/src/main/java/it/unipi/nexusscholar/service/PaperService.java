package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import java.util.List;

/**
 * Service interface for managing Paper entities.
 */
public interface PaperService {

    // --- CREATE / UPDATE ---

    /**
     * Saves or updates a Paper.
     * If the ID is present, it attempts an update.
     * If ID is null, it creates a new Paper.
     *
     * @param paperDTO The paper data.
     * @return The saved paper as DTO.
     */
    PaperDTO savePaper(PaperDTO paperDTO);

    // --- READ ---

    /**
     * Retrieves a paper by its internal MongoDB ID.
     *
     * @param id The internal ID.
     * @return The found PaperDTO.
     */
    PaperDTO getPaperById(String id);

    /**
     * Searches for papers by title.
     *
     * @param title The partial title.
     * @return List of matching PaperDTOs.
     */
    List<PaperDTO> searchPapersByTitle(String title);

    /**
     * Retrieves papers by a specific year.
     *
     * @param year The year to filter by.
     * @return List of PaperDTOs.
     */
    List<PaperDTO> getPapersByYear(Integer year);

    // --- DELETE ---

    /**
     * Deletes a paper by its ID.
     *
     * @param id The internal ID of the paper to delete.
     */
    void deletePaper(String id);
}