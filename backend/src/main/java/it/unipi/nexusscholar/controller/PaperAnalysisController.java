package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.service.PaperAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for paper analytical queries. Endpoints: /api/papers/analysis
 *
 * <p>All endpoints use MongoDB aggregation pipelines for efficient analytics.
 */
@RestController
@RequestMapping("/api/papers/analysis")
@RequiredArgsConstructor
public class PaperAnalysisController {

  private final PaperAnalysisService paperAnalysisService;

  /**
   * Analyzes publication trends by field of study per year. GET /api/papers/analysis/trend
   *
   * <p>Identifies emerging research trends (Hot Topics).
   */
  @GetMapping("/trend")
  public ResponseEntity<List<TrendAnalysisDTO>> getTrendAnalysis() {
    List<TrendAnalysisDTO> result = paperAnalysisService.getTrendAnalysis();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Ranks venues by publication count per year. GET /api/papers/analysis/venue
   *
   * <p>Useful for identifying high-impact venues.
   */
  @GetMapping("/venue")
  public ResponseEntity<List<VenueAnalysisDTO>> getVenueAnalysis() {
    List<VenueAnalysisDTO> result = paperAnalysisService.getVenueAnalysis();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Tracks collaboration evolution (average authors per paper) over time. GET
   * /api/papers/analysis/collaboration
   */
  @GetMapping("/collaboration")
  public ResponseEntity<List<CollaborationEvolutionDTO>> getCollaborationEvolution() {
    List<CollaborationEvolutionDTO> result = paperAnalysisService.getCollaborationEvolution();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
