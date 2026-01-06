package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.service.PaperService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for Paper CRUD operations. Endpoints: /api/papers */
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

  private final PaperService paperService;

  // --- CREATE / UPDATE ---

  /**
   * Creates or updates a paper. POST /api/papers
   *
   * <p>Validates author references exist, enforces DOI uniqueness, and maintains bidirectional
   * consistency with Author collection.
   */
  @PostMapping
  public ResponseEntity<PaperDTO> savePaper(@RequestBody PaperDTO paperDTO) {
    PaperDTO savedPaper = paperService.savePaper(paperDTO);
    return ResponseEntity.ok(savedPaper);
  }

  // --- READ ---

  /** Retrieves a paper by MongoDB ID. GET /api/papers/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<PaperDTO> getPaperById(@PathVariable String id) {
    return ResponseEntity.ok(paperService.getPaperById(id));
  }

  /**
   * Searches papers by title (partial match, case-insensitive). GET /api/papers/search?title=Deep
   * Learning
   */
  @GetMapping("/search")
  public ResponseEntity<List<PaperDTO>> searchPapersByTitle(@RequestParam String title) {
    List<PaperDTO> papers = paperService.searchPapersByTitle(title);
    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  /** Retrieves papers by publication year. GET /api/papers/year/2023 */
  @GetMapping("/year/{year}")
  public ResponseEntity<List<PaperDTO>> getPapersByYear(@PathVariable Integer year) {
    List<PaperDTO> papers = paperService.getPapersByYear(year);
    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  // --- DELETE ---

  /**
   * Deletes a paper by ID. DELETE /api/papers/{id}
   *
   * <p>Side effect: Removes paper from all associated authors' publication histories.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePaper(@PathVariable String id) {
    paperService.deletePaper(id);
    return ResponseEntity.noContent().build();
  }
}
