package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    // --- CREATE / UPDATE ---

    /**
     * Creates a new author or updates an existing one based on the S2 Author ID.
     * Note: For new insertions, the publication summary list must be empty.
     *
     * @param authorDTO The author data payload.
     * @return The saved AuthorDTO or an error message.
     */
    @PostMapping
    public ResponseEntity<?> saveAuthor(@RequestBody AuthorDTO authorDTO) {
        try {
            AuthorDTO savedAuthor = authorService.saveAuthor(authorDTO);
            return new ResponseEntity<>(savedAuthor, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Returns 400 Bad Request if the business rule (empty list for new authors) is violated
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- READ ---

    /**
     * Retrieves a single author by their Semantic Scholar ID.
     *
     * @param s2AuthorId The external ID to search for.
     * @return The AuthorDTO if found.
     */
    @GetMapping("/{s2AuthorId}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable String s2AuthorId) {
        // If the service throws RuntimeException (Not Found), it will be handled by the global handler (or 500)
        // Ideally, you should have a GlobalExceptionHandler, but this works for now.
        return ResponseEntity.ok(authorService.getAuthorByS2Id(s2AuthorId));
    }

    /**
     * Searches for authors by name (partial match).
     * Usage: GET /api/authors/search?name=doe
     *
     * @param name The name fragment to search.
     * @return List of matching authors.
     */
    @GetMapping("/search")
    public ResponseEntity<List<AuthorDTO>> searchAuthorsByName(@RequestParam String name) {
        List<AuthorDTO> authors = authorService.searchAuthorsByName(name);
        if (authors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(authors);
    }

    /**
     * Retrieves authors with more than a specific number of publications.
     * Usage: GET /api/authors/stats/min-publications?min=10
     *
     * @param min The minimum number of publications.
     * @return List of matching authors.
     */
    @GetMapping("/stats/min-publications")
    public ResponseEntity<List<AuthorDTO>> getAuthorsByMinPublications(@RequestParam Integer min) {
        List<AuthorDTO> authors = authorService.getAuthorsWithMinPublications(min);
        if (authors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(authors);
    }

    // --- DELETE ---

    /**
     * Deletes an author by their Semantic Scholar ID.
     *
     * @param s2AuthorId The ID of the author to delete.
     * @return 204 No Content if successful.
     */
    @DeleteMapping("/{s2AuthorId}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable String s2AuthorId) {
        authorService.deleteAuthorByS2Id(s2AuthorId);
        return ResponseEntity.noContent().build();
    }
}