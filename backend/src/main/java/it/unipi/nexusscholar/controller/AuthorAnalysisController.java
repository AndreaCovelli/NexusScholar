package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors/analysis") // Base URL per gli autori
public class AuthorAnalysisController {

    private final AuthorAnalysisService authorAnalysisService;

    @Autowired
    public AuthorAnalysisController(AuthorAnalysisService authorAnalysisService) {
        this.authorAnalysisService = authorAnalysisService;
    }


    // 1. Endpoint per trovare gli autori prolifici
    // Esempio URL: GET http://localhost:8080/api/authors/prolific?minPublications=5
    @GetMapping("/prolific")
    public ResponseEntity<List<ProlificAuthorDTO>> getProlificAuthors(
            @RequestParam(name = "minPublications", defaultValue = "10") int minPublications) {
        try {
            List<ProlificAuthorDTO> authors = authorAnalysisService.getProlificAuthors(minPublications);

            if (authors.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 se non trova nessuno
            }

            return new ResponseEntity<>(authors, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
