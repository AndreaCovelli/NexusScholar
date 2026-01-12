package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import java.util.List;

/**
 * Interface defining complex analytical operations on Paper data.
 * <p>
 * These methods typically involve heavy aggregations and grouping operations
 * that go beyond simple CRUD queries.
 * </p>
 */
public interface PaperAnalysisDAO {

  /**
   * Performs a "Hot Topics" analysis to identify trending fields of study.
   * <p>
   * Calculates the number of papers published per field of study for each year.
   * </p>
   *
   * @return A list of {@link TrendAnalysis} results.
   * @throws DAOException If the aggregation fails.
   */
  List<TrendAnalysis> getTrendAnalysis() throws DAOException;

  /**
   * Performs a Venue Impact analysis.
   * <p>
   * Calculates the number of papers published in each venue for each year,
   * helping to identify high-volume or prestigious venues.
   * </p>
   *
   * @return A list of {@link VenueAnalysis} results.
   * @throws DAOException If the aggregation fails.
   */
  List<VenueAnalysis> getVenueAnalysis() throws DAOException;

  /**
   * Analyzes the evolution of collaboration patterns over time.
   * <p>
   * Calculates the average number of authors per paper for each year.
   * </p>
   *
   * @return A list of {@link CollaborationEvolution} results.
   * @throws DAOException If the aggregation fails.
   */
  List<CollaborationEvolution> getCollaborationEvolution() throws DAOException;
}