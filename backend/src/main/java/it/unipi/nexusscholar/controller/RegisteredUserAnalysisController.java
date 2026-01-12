package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import it.unipi.nexusscholar.service.RegisteredUserAnalysisService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for registered user analytical queries.
 * <p>
 * Handles operations related to statistics and data aggregation, such as
 * generating leaderboards based on user activity.
 * </p>
 */
@RestController
@RequestMapping("/api/users/analysis")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Analysis", description = "Endpoints for user statistics and paper leaderboards")
public class RegisteredUserAnalysisController {

    private final RegisteredUserAnalysisService registeredUserAnalysisService;

    /**
     * Retrieves the leaderboard of the most important papers for a specific month.
     * <p>
     * Calculates the ranking based on the number of bookmarks received by papers
     * within the specified year and month.
     * </p>
     *
     * @param year  The year to filter (must be >= 1900).
     * @param month The month to filter (1-12).
     * @return A list of the top 5 papers.
     */
    @Operation(
            summary = "Get Monthly Paper Leaderboard",
            description = "Returns the top 5 most bookmarked papers for a specific month and year. " +
                    "Requires a user with role 'USER'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No data found for the specified period"),
            @ApiResponse(responseCode = "400", description = "Invalid year or month provided"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid or missing token)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Requires 'USER' role)")
    })
    @GetMapping("/leaderboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PaperLeaderboardDTO>> getPaperLeaderboard(
            @Parameter(description = "Year of the leaderboard (e.g., 2025)", example = "2025")
            @RequestParam(name = "year") @Min(value = 1900, message = "Year must be valid") int year,

            @Parameter(description = "Month of the leaderboard (1-12)", example = "11")
            @RequestParam(name = "month")
            @Min(value = 1, message = "Month must be at least 1")
            @Max(value = 12, message = "Month must be at most 12")
            int month) {

        List<PaperLeaderboardDTO> leaderboard =
                registeredUserAnalysisService.getPaperLeaderboard(year, month);

        if (leaderboard.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(leaderboard);
    }
}