package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.service.PaperService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

  private final PaperService paperService;

  // --- CREATE / UPDATE ---

  @PostMapping
  public ResponseEntity<?> savePaper(@RequestBody PaperDTO paperDTO) {
    try {
      PaperDTO savedPaper = paperService.savePaper(paperDTO);
      return new ResponseEntity<>(savedPaper, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      // Handles duplicate DOI or other validation errors
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  // --- READ ---

  @GetMapping("/{id}")
  public ResponseEntity<PaperDTO> getPaperById(@PathVariable String id) {
    return ResponseEntity.ok(paperService.getPaperById(id));
  }

  @GetMapping("/search")
  public ResponseEntity<List<PaperDTO>> searchPapersByTitle(@RequestParam String title) {
    List<PaperDTO> papers = paperService.searchPapersByTitle(title);
    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  @GetMapping("/year/{year}")
  public ResponseEntity<List<PaperDTO>> getPapersByYear(@PathVariable Integer year) {
    List<PaperDTO> papers = paperService.getPapersByYear(year);
    if (papers.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(papers);
  }

  // --- DELETE ---

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePaper(@PathVariable String id) {
    paperService.deletePaper(id);
    return ResponseEntity.noContent().build();
  }
}
