package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for Paper CRUD operations. Endpoints: /api/papers */
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

  private final PaperService paperService;

  // --- CREATE / UPDATE ---

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<PaperDTO> savePaper(@RequestBody PaperDTO paperDTO) {
    PaperDTO savedPaper = paperService.savePaper(paperDTO);
    return ResponseEntity.ok(savedPaper);
  }

  // --- READ ---

  @GetMapping("/{id}")
  public ResponseEntity<PaperDTO> getPaperById(@PathVariable String id) {
    return ResponseEntity.ok(paperService.getPaperById(id));
  }

  /**
   * Searches papers by title with pagination. Spring automatically resolves page, size, and sort
   * parameters into the Pageable object. Example: GET
   * /api/papers/search?title=Deep&page=0&size=10&sort=year,desc
   */
  @GetMapping("/search")
  public ResponseEntity<Page<PaperDTO>> searchPapersByTitle(
      @RequestParam String title, @PageableDefault(size = 10) Pageable pageable) {

    Page<PaperDTO> papers = paperService.searchPapersByTitle(title, pageable);

    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  /**
   * Searches papers smartly with pagination. Spring automatically resolves page, size, and sort
   * parameters into the Pageable object. Example: GET
   * /api/papers/smart-search?keyword=Deep&page=0&size=10&sort=year,desc
   */
  @GetMapping("/smart-search")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<PaperDTO>> smartSearch(
          @RequestParam String keyword, @PageableDefault(size = 10) Pageable pageable) {

    Page<PaperDTO> papers = paperService.searchPapersByText(keyword, pageable);

    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  /**
   * Retrieves papers by publication year with pagination. Example: GET
   * /api/papers/year/2023?page=0&size=20
   */
  @GetMapping("/year/{year}")
  public ResponseEntity<Page<PaperDTO>> getPapersByYear(
      @PathVariable Integer year, @PageableDefault(size = 10) Pageable pageable) {

    Page<PaperDTO> papers = paperService.getPapersByYear(year, pageable);

    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  // --- DELETE ---

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Void> deletePaper(@PathVariable String id) {
    paperService.deletePaper(id);
    return ResponseEntity.noContent().build();
  }
}
