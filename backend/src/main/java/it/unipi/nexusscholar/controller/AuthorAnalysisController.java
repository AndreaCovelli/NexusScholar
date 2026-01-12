package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for author analytical queries.
 * <p>
 * Provides endpoints to analyze author performance and statistics using MongoDB aggregation pipelines.
 * </p>
 */
@RestController
@RequestMapping("/api/authors/analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "Author Analysis", description = "Endpoints for statistical analysis on authors")
public class AuthorAnalysisController {

  private final AuthorAnalysisService authorAnalysisService;

  /**
   * Identifies prolific authors based on a publication count threshold.
   * <p>
   * This endpoint aggregates publication data to find authors who have published
   * more papers than the specified minimum in a year.
   * </p>
   *
   * @param minPublications The minimum number of publications required (default: 10).
   * @return A list of authors meeting the criteria.
   */
  @Operation(
          summary = "Identify prolific authors",
          description = "Retrieves a list of authors who have published more than the specified number of papers."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Successfully retrieved the list of prolific authors",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProlificAuthorDTO.class))
          ),
          @ApiResponse(responseCode = "204", description = "No authors found exceeding the threshold"),
          @ApiResponse(responseCode = "400", description = "Invalid minimum publications value provided"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
  })
  @GetMapping("/prolific")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<List<ProlificAuthorDTO>> getProlificAuthors(
          @Parameter(description = "Minimum publication count threshold", example = "5")
          @RequestParam(name = "minPublications", defaultValue = "10")
          @Min(value = 1, message = "minPublications must be at least 1")
          int minPublications) {

    List<ProlificAuthorDTO> authors = authorAnalysisService.getProlificAuthors(minPublications);

    if (authors.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(authors, HttpStatus.OK);
  }
}