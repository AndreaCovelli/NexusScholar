package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import java.util.List;

/**
 * Interface defining analytical operations on Registered Users.
 * <p>
 * This DAO focuses on complex queries and aggregations, such as generating leaderboards
 * or statistical analysis of user activities (e.g., bookmarks).
 * </p>
 */
public interface RegisteredUserAnalysisDAO {

  /**
   * Retrieves the leaderboard of the most bookmarked papers for a specific month and year.
   * <p>
   * This method aggregates user data to determine which papers have been saved the most
   * within the specified time frame.
   * </p>
   *
   * @param year  The year to filter by (e.g., 2025).
   * @param month The month to filter by (e.g., 11 for November).
   * @return A list of {@link PaperLeaderboard} objects containing the top 5 papers.
   */
  List<PaperLeaderboard> getMostBookmarkedPapers(int year, int month);
}