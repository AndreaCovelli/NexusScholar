package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for exposing graph analysis functionalities. Provides endpoints for PageRank,
 * Shortest Path, Community Detection, and Betweenness Centrality.
 */
@RestController
@RequestMapping("/api/graph/analysis")
@RequiredArgsConstructor
@Tag(
    name = "Graph Analysis",
    description = "API for executing analysis algorithms on the Neo4j graph")
public class GraphController {

  private final GraphService graphService;

  /**
   * Endpoint to retrieve PageRank calculations for papers. Identifies the most influential papers
   * based on citations.
   *
   * @param pageable Pagination parameters (default size: 20, sort: rank DESC).
   * @return A page of PageRank results.
   */
  @Operation(
      summary = "PageRank Computation",
      description = "Returns a paginated list of papers ordered by their PageRank score.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Operation completed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageRankDTO.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied (requires USER role)",
            content = @Content)
      })
  @GetMapping("/pagerank")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<PageRankDTO>> callPageRank(
      @Parameter(
              description = "Pagination parameters",
              example = "{\"page\": 0, \"size\": 20, \"sort\": [\"rank,desc\"]}")
          @PageableDefault(size = 20, sort = "rank", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(graphService.pagerank(pageable));
  }

  /**
   * Endpoint to calculate the shortest path between two authors.
   *
   * @param a1 Name of the first author (start node).
   * @param a2 Name of the second author (end node).
   * @return The detailed shortest path with nodes and relationships.
   */
  @Operation(
      summary = "Shortest Path",
      description = "Calculates the shortest collaboration path between two specified authors.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Path found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShortestPathDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "No path found between the specified authors",
            content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
      })
  @GetMapping("/shortestPath")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ShortestPathDTO> callShortestPath(
      @Parameter(description = "Name of the first author", required = true) @RequestParam String a1,
      @Parameter(description = "Name of the second author", required = true) @RequestParam
          String a2) {
    ShortestPathDTO res = graphService.collabPath(a1, a2);
    if (res == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(res);
  }

  /**
   * Endpoint to retrieve author communities (Leiden Algorithm).
   *
   * @param pageable Pagination parameters.
   * @return A page of identified communities.
   */
  @Operation(
      summary = "Community Detection (Leiden)",
      description =
          "Executes the Leiden algorithm to identify communities of authors who frequently collaborate.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Communities retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LeidenDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
      })
  @GetMapping("/leidenCommunities")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<LeidenDTO>> callLeiden(
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 20)
          Pageable pageable) {
    return ResponseEntity.ok(graphService.hiddenCommunities(pageable));
  }

  /**
   * Endpoint to calculate Betweenness Centrality. Identifies "bridge" or crucial nodes in the
   * graph's information flow.
   *
   * @param pageable Pagination parameters (default size: 20, sort: betweenness DESC).
   * @return A page of Betweenness results.
   */
  @Operation(
      summary = "Betweenness Centrality",
      description = "Calculates and returns nodes with the highest betweenness centrality.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Calculation completed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BetweennessDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
      })
  @GetMapping("/betweenness")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<BetweennessDTO>> callBetweenness(
      @Parameter(description = "Pagination parameters")
          @PageableDefault(size = 20, sort = "betweenness", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(graphService.betweenness(pageable));
  }
}
