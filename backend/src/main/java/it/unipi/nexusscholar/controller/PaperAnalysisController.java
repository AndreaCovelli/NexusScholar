package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.service.PaperAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for paper analytical queries.
 * <p>
 * This controller provides endpoints to retrieve statistical data and trends
 * regarding scientific papers, venues, and collaborations using MongoDB aggregation pipelines.
 * </p>
 */
@RestController
@RequestMapping("/api/papers/analysis")
@RequiredArgsConstructor
@Tag(name = "Paper Analysis", description = "Endpoints for statistical analysis and trends on scientific papers")
public class PaperAnalysisController {

  private final PaperAnalysisService paperAnalysisService;

  /**
   * Analyzes publication trends by field of study per year.
   * <p>
   * This endpoint aggregates data to identify emerging research trends (Hot Topics)
   * by counting papers in each field of study for every year.
   * </p>
   *
   * @return A list of {@link TrendAnalysisDTO} representing the volume of papers per topic per year.
   */
  @Operation(
          summary = "Get trend analysis",
          description = "Retrieves the number of papers created per field of study for each year to identify emerging trends."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Successfully retrieved trend analysis",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrendAnalysisDTO.class))
          ),
          @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated"),
          @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the required role")
  })
  @GetMapping("/trend")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<List<TrendAnalysisDTO>> getTrendAnalysis() {
    List<TrendAnalysisDTO> result = paperAnalysisService.getTrendAnalysis();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Ranks venues by publication count per year.
   * <p>
   * This endpoint is useful for identifying high-impact venues (conferences or journals)
   * based on the volume of publications over time.
   * </p>
   *
   * @return A list of {@link VenueAnalysisDTO} containing venue names and their paper counts per year.
   */
  @Operation(
          summary = "Get venue analysis",
          description = "Ranks publication venues (conferences/journals) based on the volume of papers published per year."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Successfully retrieved venue analysis",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = VenueAnalysisDTO.class))
          ),
          @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated"),
          @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the required role")
  })
  @GetMapping("/venue")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<List<VenueAnalysisDTO>> getVenueAnalysis() {
    List<VenueAnalysisDTO> result = paperAnalysisService.getVenueAnalysis();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Tracks collaboration evolution (average authors per paper) over time.
   * <p>
   * This endpoint calculates the average team size for papers published in each year
   * to visualize how scientific collaboration has changed.
   * </p>
   *
   * @return A list of {@link CollaborationEvolutionDTO} showing the average number of authors per year.
   */
  @Operation(
          summary = "Get collaboration evolution",
          description = "Calculates the average number of authors per paper over the years to track collaboration trends."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Successfully retrieved collaboration stats",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollaborationEvolutionDTO.class))
          ),
          @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated"),
          @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the required role")
  })
  @GetMapping("/collaboration")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<List<CollaborationEvolutionDTO>> getCollaborationEvolution() {
    List<CollaborationEvolutionDTO> result = paperAnalysisService.getCollaborationEvolution();
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}