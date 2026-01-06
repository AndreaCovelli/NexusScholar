package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import java.util.List;

/**
 * Data access interface for paper-related analytical queries. These operations use MongoDB
 * aggregation pipelines for complex analytics.
 */
public interface PaperAnalysisDAO {

  /**
   * Trend Analysis (Hot Topics): Calculates paper counts per field of study per year. Identifies
   * emerging research trends across time.
   *
   * @return List of trend analysis results ordered by year ascending
   * @throws DAOException if database operation fails
   */
  List<TrendAnalysis> getTrendAnalysis() throws DAOException;

  /**
   * Venue Impact Analysis: Ranks venues by paper count per year. Useful for identifying high-impact
   * publication venues.
   *
   * @return List of venue analysis results ordered by year and paper count
   * @throws DAOException if database operation fails
   */
  List<VenueAnalysis> getVenueAnalysis() throws DAOException;

  /**
   * Collaboration Evolution: Computes average author count per paper by year. Tracks how research
   * collaboration patterns evolve over time.
   *
   * @return List of collaboration metrics ordered by year ascending
   * @throws DAOException if database operation fails
   */
  List<CollaborationEvolution> getCollaborationEvolution() throws DAOException;
}
