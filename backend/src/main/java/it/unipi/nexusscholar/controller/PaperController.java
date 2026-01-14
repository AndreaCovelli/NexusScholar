package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Paper CRUD operations.
 *
 * <p>Endpoints: /api/papers
 */
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
@Tag(name = "Paper Controller", description = "REST controller for Paper CRUD operations")
public class PaperController {

  private final PaperService paperService;

  // --- CREATE / UPDATE ---

  /**
   * Creates or updates a paper.
   *
   * <p>POST /api/papers
   *
   * @param paperDTO The paper data transfer object
   * @return The saved paper
   */
  @Operation(summary = "Creates or updates a paper")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Paper saved successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error (e.g., missing authors)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role")
      })
  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<PaperDTO> savePaper(@RequestBody PaperDTO paperDTO) {
    PaperDTO savedPaper = paperService.savePaper(paperDTO);
    return ResponseEntity.ok(savedPaper);
  }

  // --- READ ---

  /**
   * Retrieves a paper by its ID.
   *
   * <p>GET /api/papers/{id}
   *
   * @param id The paper ID
   * @return The requested paper
   */
  @Operation(summary = "Retrieves a paper by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Paper found"),
        @ApiResponse(responseCode = "404", description = "Paper not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<PaperDTO> getPaperById(
      @Parameter(description = "The Paper ID") @PathVariable String id) {
    return ResponseEntity.ok(paperService.getPaperById(id));
  }

  /**
   * Searches papers by title with pagination.
   *
   * <p>Spring automatically resolves page, size, and sort parameters into the Pageable object.
   *
   * <p>Example: GET /api/papers/search?title=Deep&page=0&size=10&sort=year,desc
   *
   * @param title The title to search for
   * @param pageable Pagination information
   * @return A page of papers matching the title, or 204 No Content if empty
   */
  @Operation(summary = "Searches papers by title with pagination")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "List of papers retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No papers found matching the title")
      })
  @GetMapping("/search")
  public ResponseEntity<Page<PaperDTO>> searchPapersByTitle(
      @Parameter(description = "Title to search for", example = "Deep Learning") @RequestParam
          String title,
      @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {

    Page<PaperDTO> papers = paperService.searchPapersByTitle(title, pageable);

    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  /**
   * Searches papers smartly with pagination (Text Search).
   *
   * <p>Spring automatically resolves page, size, and sort parameters into the Pageable object.
   *
   * <p>Example: GET /api/papers/smart-search?keyword=Deep&page=0&size=10&sort=year,desc
   *
   * @param keyword The keyword to search for in text
   * @param pageable Pagination information
   * @return A page of papers matching the keyword, or 204 No Content if empty
   */
  @Operation(summary = "Searches papers smartly with pagination (Text Search)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "List of papers retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No papers found matching the keyword"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role")
      })
  @GetMapping("/smart-search")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<PaperDTO>> smartSearch(
      @Parameter(description = "Keyword for text search", example = "Neural Networks") @RequestParam
          String keyword,
      @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {

    Page<PaperDTO> papers = paperService.searchPapersByText(keyword, pageable);

    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  /**
   * Retrieves papers by publication year with pagination.
   *
   * <p>Example: GET /api/papers/year/2023?page=0&size=20
   *
   * @param year The publication year
   * @param pageable Pagination information
   * @return A page of papers published in that year, or 204 No Content if empty
   */
  @Operation(summary = "Retrieves papers by publication year with pagination")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "List of papers retrieved successfully"),
        @ApiResponse(responseCode = "204", description = "No papers found for this year")
      })
  @GetMapping("/year/{year}")
  public ResponseEntity<Page<PaperDTO>> getPapersByYear(
      @Parameter(description = "Publication year", example = "2023") @PathVariable Integer year,
      @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {

    Page<PaperDTO> papers = paperService.getPapersByYear(year, pageable);

    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  // --- DELETE ---

  /**
   * Deletes a paper by its ID.
   *
   * <p>DELETE /api/papers/{id}
   *
   * @param id The paper ID
   * @return No Content response
   */
  @Operation(summary = "Deletes a paper by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Paper deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Paper not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role")
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> deletePaper(
      @Parameter(description = "The Paper ID") @PathVariable String id) {
    paperService.deletePaper(id);
    return ResponseEntity.noContent().build();
  }
}
