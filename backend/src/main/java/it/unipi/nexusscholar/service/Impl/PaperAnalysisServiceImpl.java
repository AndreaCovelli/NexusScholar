package it.unipi.nexusscholar.service.Impl;

import it.unipi.nexusscholar.dao.PaperAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import it.unipi.nexusscholar.service.PaperAnalysisService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaperAnalysisServiceImpl implements PaperAnalysisService {

  private final PaperAnalysisDAO paperAnalysisDAO;

  private PaperDTO toPaperDTO(Paper paper) {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setId(paper.getId());
    paperDTO.setTitle(paper.getTitle());
    paperDTO.setYear(paper.getYear());
    paperDTO.setDoi(paper.getDoi());
    paperDTO.setAuthors(paper.getAuthors());
    paperDTO.setAbstractText(paper.getAbstractText());
    paperDTO.setDblpKey(paper.getDblpKey());
    paperDTO.setFieldsOfStudy(paper.getFieldsOfStudy());
    paperDTO.setVenue(paper.getVenue());
    return paperDTO;
  }

  private TrendAnalysisDTO toTrendAnalysisDTO(TrendAnalysis trendAnalysis) {
    TrendAnalysisDTO trendAnalysisDTO = new TrendAnalysisDTO();
    trendAnalysisDTO.setFieldOfStudy(trendAnalysis.getFieldOfStudy());
    trendAnalysisDTO.setYear(trendAnalysis.getYear());
    trendAnalysisDTO.setPaperCreated(trendAnalysis.getPaperCreated());
    return trendAnalysisDTO;
  }

  private VenueAnalysisDTO toVenueAnalysisDTO(VenueAnalysis venueAnalysis) {
    VenueAnalysisDTO venueAnalysisDTO = new VenueAnalysisDTO();
    venueAnalysisDTO.setPaperCreated(venueAnalysis.getPaperCreated());
    venueAnalysisDTO.setYear(venueAnalysis.getYear());
    venueAnalysisDTO.setVenue(venueAnalysis.getVenue());
    return venueAnalysisDTO;
  }

  private CollaborationEvolutionDTO toCollaborationEvolutionDTO(
      CollaborationEvolution collaborationEvolution) {
    CollaborationEvolutionDTO collaborationEvolutionDTO = new CollaborationEvolutionDTO();
    collaborationEvolutionDTO.setAvgAuthors(collaborationEvolution.getAvgAuthors());
    collaborationEvolutionDTO.setYear(collaborationEvolution.getYear());
    return collaborationEvolutionDTO;
  }

  @Autowired
  public PaperAnalysisServiceImpl(PaperAnalysisDAO paperAnalysisDAO) {
    this.paperAnalysisDAO = paperAnalysisDAO;
  }

  @Override
  public List<VenueAnalysisDTO> getVenueAnalysis() {
    List<VenueAnalysis> Venues = paperAnalysisDAO.getVenueAnalysis();
    return Venues.stream().map(this::toVenueAnalysisDTO).toList();
  }

  @Override
  public List<CollaborationEvolutionDTO> getCollaborationEvolution() {
    List<CollaborationEvolution> Collabs = paperAnalysisDAO.getCollaborationEvolution();
    return Collabs.stream().map(this::toCollaborationEvolutionDTO).toList();
  }

  @Override
  public List<TrendAnalysisDTO> getTrendAnalysis() {
    List<TrendAnalysis> Trends = paperAnalysisDAO.getTrendAnalysis();
    return Trends.stream().map(this::toTrendAnalysisDTO).toList();
  }
}
