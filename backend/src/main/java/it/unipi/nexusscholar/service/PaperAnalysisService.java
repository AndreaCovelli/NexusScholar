package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import java.util.List;

/** Service interface for paper-related analytical operations. */
public interface PaperAnalysisService {

  /** Analyzes paper publication trends by field of study and year. */
  List<TrendAnalysisDTO> getTrendAnalysis();

  /** Ranks venues by paper count per year. */
  List<VenueAnalysisDTO> getVenueAnalysis();

  /** Tracks collaboration patterns (average authors per paper) over time. */
  List<CollaborationEvolutionDTO> getCollaborationEvolution();
}
