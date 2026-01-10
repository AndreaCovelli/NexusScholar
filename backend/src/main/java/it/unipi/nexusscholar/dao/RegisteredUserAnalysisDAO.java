package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import java.util.List;

public interface RegisteredUserAnalysisDAO {

    /**
     * Retrieves the leaderboard of the most bookmarked papers for a specific month and year.
     *
     * @param year  The year to filter by (e.g., 2025).
     * @param month The month to filter by (e.g., 11 for November).
     * @return A list of the top 5 papers with the highest number of bookmarks.
     */
    List<PaperLeaderboard> getMostBookmarkedPapers(int year, int month);
}