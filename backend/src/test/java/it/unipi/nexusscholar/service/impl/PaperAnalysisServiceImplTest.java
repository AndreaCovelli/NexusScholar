package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.PaperAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaperAnalysisServiceImplTest {

  @Mock private PaperAnalysisDAO paperAnalysisDAO;

  @InjectMocks private PaperAnalysisServiceImpl paperAnalysisService;

  @Test
  void getTrendAnalysis_ReturnsResults() {
    TrendAnalysis trend1 = new TrendAnalysis("Machine Learning", 2023, 150);
    TrendAnalysis trend2 = new TrendAnalysis("Deep Learning", 2023, 200);

    when(paperAnalysisDAO.getTrendAnalysis()).thenReturn(List.of(trend1, trend2));

    List<TrendAnalysisDTO> results = paperAnalysisService.getTrendAnalysis();

    assertEquals(2, results.size());
    assertEquals("Machine Learning", results.get(0).getFieldOfStudy());
    assertEquals(2023, results.get(0).getYear());
    assertEquals(150, results.get(0).getPaperCreated());
  }

  @Test
  void getTrendAnalysis_EmptyResults() {
    when(paperAnalysisDAO.getTrendAnalysis()).thenReturn(Collections.emptyList());

    List<TrendAnalysisDTO> results = paperAnalysisService.getTrendAnalysis();

    assertTrue(results.isEmpty());
  }

  @Test
  void getVenueAnalysis_ReturnsResults() {
    VenueAnalysis venue1 = new VenueAnalysis("NeurIPS", 2023, 500);
    VenueAnalysis venue2 = new VenueAnalysis("ICML", 2023, 400);

    when(paperAnalysisDAO.getVenueAnalysis()).thenReturn(List.of(venue1, venue2));

    List<VenueAnalysisDTO> results = paperAnalysisService.getVenueAnalysis();

    assertEquals(2, results.size());
    assertEquals("NeurIPS", results.get(0).getVenue());
    assertEquals(2023, results.get(0).getYear());
    assertEquals(500, results.get(0).getPaperCreated());
  }

  @Test
  void getVenueAnalysis_EmptyResults() {
    when(paperAnalysisDAO.getVenueAnalysis()).thenReturn(Collections.emptyList());

    List<VenueAnalysisDTO> results = paperAnalysisService.getVenueAnalysis();

    assertTrue(results.isEmpty());
  }

  @Test
  void getCollaborationEvolution_ReturnsResults() {
    CollaborationEvolution collab1 = new CollaborationEvolution(3.5, 2022);
    CollaborationEvolution collab2 = new CollaborationEvolution(4.2, 2023);

    when(paperAnalysisDAO.getCollaborationEvolution()).thenReturn(List.of(collab1, collab2));

    List<CollaborationEvolutionDTO> results = paperAnalysisService.getCollaborationEvolution();

    assertEquals(2, results.size());
    assertEquals(3.5, results.get(0).getAvgAuthors(), 0.001);
    assertEquals(2022, results.get(0).getYear());
    assertEquals(4.2, results.get(1).getAvgAuthors(), 0.001);
  }

  @Test
  void getCollaborationEvolution_EmptyResults() {
    when(paperAnalysisDAO.getCollaborationEvolution()).thenReturn(Collections.emptyList());

    List<CollaborationEvolutionDTO> results = paperAnalysisService.getCollaborationEvolution();

    assertTrue(results.isEmpty());
  }
}
