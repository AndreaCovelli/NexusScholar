package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import java.util.List;

/**
 * Service interface for paper-related analytical operations.
 *
 * <p>Decouples the controller from the complex aggregation logic implemented in the DAO layer.
 * Transforms raw statistical data into DTOs suitable for charts and reports.
 */
public interface PaperAnalysisService {

  /**
   * Analyzes publication trends to identify "Hot Topics".
   *
   * @return A list of metrics showing paper counts per field of study per year.
   */
  List<TrendAnalysisDTO> getTrendAnalysis();

  /**
   * Ranks publication venues (Conferences/Journals) by volume.
   *
   * @return A list of metrics showing paper counts per venue per year.
   */
  List<VenueAnalysisDTO> getVenueAnalysis();

  /**
   * Tracks the evolution of scientific collaboration.
   *
   * @return A list of metrics showing the average number of authors per paper over time.
   */
  List<CollaborationEvolutionDTO> getCollaborationEvolution();
}
