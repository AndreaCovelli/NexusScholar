package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.PaperAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import it.unipi.nexusscholar.service.PaperAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaperAnalysisServiceImpl implements PaperAnalysisService {

  private final PaperAnalysisDAO paperAnalysisDAO;

  @Override
  public List<TrendAnalysisDTO> getTrendAnalysis() {
    List<TrendAnalysis> trends = paperAnalysisDAO.getTrendAnalysis();
    return trends.stream().map(this::toTrendAnalysisDTO).toList();
  }

  @Override
  public List<VenueAnalysisDTO> getVenueAnalysis() {
    List<VenueAnalysis> venues = paperAnalysisDAO.getVenueAnalysis();
    return venues.stream().map(this::toVenueAnalysisDTO).toList();
  }

  @Override
  public List<CollaborationEvolutionDTO> getCollaborationEvolution() {
    List<CollaborationEvolution> collabs = paperAnalysisDAO.getCollaborationEvolution();
    return collabs.stream().map(this::toCollaborationEvolutionDTO).toList();
  }

  private TrendAnalysisDTO toTrendAnalysisDTO(TrendAnalysis entity) {
    TrendAnalysisDTO dto = new TrendAnalysisDTO();
    dto.setFieldOfStudy(entity.getFieldOfStudy());
    dto.setYear(entity.getYear());
    dto.setPaperCreated(entity.getPaperCreated());
    return dto;
  }

  private VenueAnalysisDTO toVenueAnalysisDTO(VenueAnalysis entity) {
    VenueAnalysisDTO dto = new VenueAnalysisDTO();
    dto.setVenue(entity.getVenue());
    dto.setYear(entity.getYear());
    dto.setPaperCreated(entity.getPaperCreated());
    return dto;
  }

  private CollaborationEvolutionDTO toCollaborationEvolutionDTO(CollaborationEvolution entity) {
    CollaborationEvolutionDTO dto = new CollaborationEvolutionDTO();
    dto.setAvgAuthors(entity.getAvgAuthors());
    dto.setYear(entity.getYear());
    return dto;
  }
}
