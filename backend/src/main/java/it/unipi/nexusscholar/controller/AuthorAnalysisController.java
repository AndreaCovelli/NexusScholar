package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** REST controller for author analytical queries. Endpoints: /api/authors/analysis */
@RestController
@RequestMapping("/api/authors/analysis")
@RequiredArgsConstructor
@Validated
public class AuthorAnalysisController {

  private final AuthorAnalysisService authorAnalysisService;

  /**
   * Identifies prolific authors with publication count exceeding threshold. GET
   * /api/authors/analysis/prolific?minPublications=5
   *
   * <p>Uses MongoDB aggregation pipeline for efficient computation.
   */
  @GetMapping("/prolific")
  public ResponseEntity<List<ProlificAuthorDTO>> getProlificAuthors(
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
