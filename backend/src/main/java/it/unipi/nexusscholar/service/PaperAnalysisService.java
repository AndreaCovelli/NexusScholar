package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface PaperAnalysisService {

  public List<VenueAnalysisDTO> getVenueAnalysis();

  public List<CollaborationEvolutionDTO> getCollaborationEvolution();

  public List<TrendAnalysisDTO> getTrendAnalysis();
}
