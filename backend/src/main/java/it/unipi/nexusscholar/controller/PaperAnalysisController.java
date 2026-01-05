package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.service.PaperAnalysisService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/papers/analysis") // Prefisso base per tutti gli endpoint di questo controller
public class PaperAnalysisController {

  private final PaperAnalysisService paperAnalysisService;

  @Autowired
  public PaperAnalysisController(PaperAnalysisService paperAnalysisService) {
    this.paperAnalysisService = paperAnalysisService;
  }

  // 1. Endpoint per l'analisi delle Venue
  // URL: GET http://localhost:8080/api/papers/venue-analysis
  @GetMapping("/venue-analysis")
  public ResponseEntity<List<VenueAnalysisDTO>> getVenueAnalysis() {
    try {
      List<VenueAnalysisDTO> result = paperAnalysisService.getVenueAnalysis();
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 2. Endpoint per l'evoluzione delle collaborazioni
  // URL: GET http://localhost:8080/api/papers/collaboration-evolution
  @GetMapping("/collaboration-evolution")
  public ResponseEntity<List<CollaborationEvolutionDTO>> getCollaborationEvolution() {
    try {
      List<CollaborationEvolutionDTO> result = paperAnalysisService.getCollaborationEvolution();
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 3. Endpoint per l'analisi dei trend
  // URL: GET http://localhost:8080/api/papers/trend-analysis
  @GetMapping("/trend-analysis")
  public ResponseEntity<List<TrendAnalysisDTO>> getTrendAnalysis() {
    try {
      List<TrendAnalysisDTO> result = paperAnalysisService.getTrendAnalysis();
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
