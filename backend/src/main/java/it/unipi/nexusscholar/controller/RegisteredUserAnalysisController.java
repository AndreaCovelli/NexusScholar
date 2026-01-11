package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import it.unipi.nexusscholar.service.RegisteredUserAnalysisService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REST controller for registered user analytical queries. Endpoints: /api/users/analysis */
@RestController
@RequestMapping("/api/users/analysis")
@RequiredArgsConstructor
@Validated
public class RegisteredUserAnalysisController {

    private final RegisteredUserAnalysisService registeredUserAnalysisService;

    /**
     * Rank of the most important papers of the month.
     * Shows the leaderboard of the most bookmarked papers in the specified month and year.
     * GET /api/users/analysis/leaderboard?year=2025&month=11
     *
     * <p>Uses MongoDB aggregation pipeline for efficient computation.
     */
    @GetMapping("/leaderboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaperLeaderboardDTO>> getPaperLeaderboard(
            @RequestParam(name = "year")
            @Min(value = 1900, message = "Year must be valid")
            int year,

            @RequestParam(name = "month")
            @Min(value = 1, message = "Month must be at least 1")
            @Max(value = 12, message = "Month must be at most 12")
            int month) {

        List<PaperLeaderboardDTO> leaderboard = registeredUserAnalysisService.getPaperLeaderboard(year, month);

        if (leaderboard.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(leaderboard, HttpStatus.OK);
    }
}