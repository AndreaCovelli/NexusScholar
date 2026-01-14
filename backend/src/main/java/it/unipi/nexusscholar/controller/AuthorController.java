package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Author CRUD operations.
 *
 * <p>Endpoints: /api/authors
 */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Author Controller", description = "REST controller for Author CRUD operations")
public class AuthorController {

  private final AuthorService authorService;

  // --- CREATE / UPDATE ---

  /**
   * Creates or updates an author.
   *
   * <p>POST /api/authors
   *
   * <p>If ID is null: Creates new author (S2 ID must be unique, no publication history allowed).
   * <br>
   * If ID exists: Updates existing author (validates paper references).
   *
   * @param authorDTO The author data transfer object
   * @return The saved author
   */
  @Operation(
      summary = "Creates or updates an author",
      description =
          "If ID is null: Creates new author (S2 ID must be unique, no publication history allowed). "
              + "If ID exists: Updates existing author (validates paper references).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Author saved successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error (e.g., duplicate S2 ID or invalid references)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role")
      })
  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<AuthorDTO> saveAuthor(@RequestBody AuthorDTO authorDTO) {
    AuthorDTO savedAuthor = authorService.saveAuthor(authorDTO);
    return ResponseEntity.ok(savedAuthor);
  }

  // --- READ ---

  /**
   * Retrieves an author by Semantic Scholar ID.
   *
   * <p>GET /api/authors/s2/{s2Id}
   *
   * @param s2Id The Semantic Scholar ID
   * @return The requested author
   */
  @Operation(summary = "Retrieves an author by Semantic Scholar ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Author found"),
        @ApiResponse(responseCode = "404", description = "Author not found")
      })
  @GetMapping("/s2/{s2Id}")
  public ResponseEntity<AuthorDTO> getAuthorByS2Id(
      @Parameter(description = "Semantic Scholar ID", example = "2053642") @PathVariable
          String s2Id) {
    AuthorDTO author = authorService.getAuthorByS2Id(s2Id);
    return ResponseEntity.ok(author);
  }

  /**
   * Searches authors by name with pagination.
   *
   * <p>GET /api/authors/search?name=Mario&page=0&size=10
   *
   * @param name The name to search for
   * @param pageable Pagination information
   * @return A page of authors matching the criteria
   */
  @Operation(summary = "Searches authors by name with pagination")
  @ApiResponse(responseCode = "200", description = "List of authors retrieved successfully")
  @GetMapping("/search")
  public ResponseEntity<Page<AuthorDTO>> searchAuthorsByName(
      @Parameter(description = "Name to search for", example = "Mario") @RequestParam String name,
      @Parameter(
              hidden = true) // Hides Pageable internal structure from Swagger UI to keep it clean
          @PageableDefault(size = 10)
          Pageable pageable) {
    Page<AuthorDTO> authors = authorService.searchAuthorsByName(name, pageable);
    return ResponseEntity.ok(authors);
  }

  /**
   * Filters authors by minimum publication count with pagination.
   *
   * <p>GET /api/authors/filter?min=10&page=0&size=10
   *
   * @param minPublications Minimum number of publications
   * @param pageable Pagination information
   * @return A page of authors meeting the criteria
   */
  @Operation(summary = "Filters authors by minimum publication count with pagination")
  @ApiResponse(responseCode = "200", description = "List of authors retrieved successfully")
  @GetMapping("/filter")
  public ResponseEntity<Page<AuthorDTO>> getAuthorsWithMinPublications(
      @Parameter(description = "Minimum number of publications", example = "10")
          @RequestParam("min")
          Integer minPublications,
      @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
    Page<AuthorDTO> authors =
        authorService.getAuthorsWithMinPublications(minPublications, pageable);
    return ResponseEntity.ok(authors);
  }

  // --- DELETE ---

  /**
   * Deletes an author by internal ID.
   *
   * <p>DELETE /api/authors/{id}
   *
   * @param id The internal Author ID
   * @return Empty response
   */
  @Operation(summary = "Deletes an author by internal ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Author deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Author not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role")
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> deleteAuthor(
      @Parameter(description = "Internal Author ID") @PathVariable String id) {
    authorService.deleteAuthorById(id);
    return ResponseEntity.ok().build();
  }
}
