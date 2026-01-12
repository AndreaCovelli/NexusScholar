package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import java.util.List;

/**
 * Service interface for analytical operations related to Registered Users.
 * <p>
 * This service handles high-level logic for generating statistics and leaderboards,
 * decoupling the controller from the data access layer.
 * </p>
 */
public interface RegisteredUserAnalysisService {

    /**
     * Retrieves a leaderboard of papers most bookmarked by users in a specific month.
     *
     * @param year  The year to analyze (e.g., 2025).
     * @param month The month to analyze (1-12).
     * @return A list of {@link PaperLeaderboardDTO} containing paper details and bookmark counts.
     */
    List<PaperLeaderboardDTO> getPaperLeaderboard(int year, int month);
}