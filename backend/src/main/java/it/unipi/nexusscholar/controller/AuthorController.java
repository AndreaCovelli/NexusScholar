package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for Author CRUD operations. Endpoints: /api/authors */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

  private final AuthorService authorService;

  // --- CREATE / UPDATE ---

  /**
   * Creates or updates an author. POST /api/authors
   *
   * <p>If ID is null: Creates new author (S2 ID must be unique, no publication history allowed).
   * <br>
   * If ID exists: Updates existing author (validates paper references).
   */
  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<AuthorDTO> saveAuthor(@RequestBody AuthorDTO authorDTO) {
    AuthorDTO savedAuthor = authorService.saveAuthor(authorDTO);
    return ResponseEntity.ok(savedAuthor);
  }

  // --- READ ---

  /** Retrieves an author by Semantic Scholar ID. GET /api/authors/s2/{s2Id} */
  @GetMapping("/s2/{s2Id}")
  public ResponseEntity<AuthorDTO> getAuthorByS2Id(@PathVariable String s2Id) {
    AuthorDTO author = authorService.getAuthorByS2Id(s2Id);
    return ResponseEntity.ok(author);
  }

  /**
   * Searches authors by name with pagination. GET
   * /api/authors/search?name=Mario&page=0&size=10
   */
  @GetMapping("/search")
  public ResponseEntity<Page<AuthorDTO>> searchAuthorsByName(
      @RequestParam String name, @PageableDefault(size = 10) Pageable pageable) {
    Page<AuthorDTO> authors = authorService.searchAuthorsByName(name, pageable);
    return ResponseEntity.ok(authors);
  }

  /**
   * Filters authors by minimum publication count with pagination. GET
   * /api/authors/filter?min=10&page=0&size=10
   */
  @GetMapping("/filter")
  public ResponseEntity<Page<AuthorDTO>> getAuthorsWithMinPublications(
      @RequestParam("min") Integer minPublications, @PageableDefault(size = 10) Pageable pageable) {
    Page<AuthorDTO> authors =
        authorService.getAuthorsWithMinPublications(minPublications, pageable);
    return ResponseEntity.ok(authors);
  }

  // --- DELETE ---

  /** Deletes an author by internal ID. DELETE /api/authors/{id} */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> deleteAuthor(@PathVariable String id) {
    authorService.deleteAuthorById(id);
    return ResponseEntity.ok().build();
  }
}
