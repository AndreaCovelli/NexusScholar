package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Administrator resources.
 *
 * <p>Provides endpoints for creating, retrieving, updating, and deleting administrator accounts.
 * Includes search functionality and token identity extraction.
 */
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Tag(
    name = "Admin Management",
    description = "APIs for managing administrator accounts, permissions, and lifecycle.")
public class AdminController {

  private final AdminService adminService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(
      summary = "Create a new Administrator",
      description =
          "Registers a new admin account with specific permissions. Checks for duplicate username or email.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Admin created successfully",
            content = @Content(schema = @Schema(implementation = AdminDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or Username/Email already exists")
      })
  @PostMapping
  public ResponseEntity<AdminDTO> createAdmin(@RequestBody AdminCreateDTO createDTO) {
    return ResponseEntity.ok(adminService.createAdmin(createDTO));
  }

  @Operation(
      summary = "Update an Administrator",
      description = "Updates username, email, password, or permissions. Requires 'ADMIN' role.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Admin updated successfully"),
        @ApiResponse(responseCode = "404", description = "Admin not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "400", description = "Email/Username already in use")
      })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AdminDTO> updateAdmin(
      @Parameter(description = "The unique identifier of the admin to update") @PathVariable
          String id,
      @RequestBody AdminUpdateDTO updateDTO) {
    return ResponseEntity.ok(adminService.updateAdmin(id, updateDTO));
  }

  @Operation(
      summary = "Get Admin by ID",
      description = "Retrieves detailed information about a specific administrator.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Admin found"),
        @ApiResponse(responseCode = "404", description = "Admin not found")
      })
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AdminDTO> getAdminById(
      @Parameter(description = "The unique identifier of the admin") @PathVariable String id) {
    return ResponseEntity.ok(adminService.getAdminById(id));
  }

  @Operation(
      summary = "List all Administrators",
      description = "Retrieves a paginated list of all registered admins.")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AdminDTO>> getAllAdmins(
      @Parameter(description = "Pagination parameters", hidden = true) @PageableDefault(size = 20)
          Pageable pageable) {
    return ResponseEntity.ok(adminService.getAllAdmins(pageable));
  }

  @Operation(
      summary = "Search Administrators by Username",
      description =
          "Searches for admins where the username starts with the provided string (Regex ^query). Uses indexed search.")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Search results retrieved")})
  @GetMapping("/search")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<AdminDTO>> searchAdmins(
      @Parameter(description = "The prefix of the username to search for") @RequestParam
          String username) {
    return ResponseEntity.ok(adminService.searchAdmins(username));
  }

  @Operation(
      summary = "Get Current User Info from Token",
      description =
          "Extracts and returns the User ID and Username directly from the provided JWT token.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Token info retrieved"),
        @ApiResponse(responseCode = "401", description = "Invalid Token")
      })
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, String>> getCurrentUser(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    String userId = jwtTokenProvider.getUserIdFromToken(token);
    String username = jwtTokenProvider.getUsernameFromToken(token);

    Map<String, String> response = new HashMap<>();
    response.put("id", userId);
    response.put("username", username);

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Delete an Administrator",
      description =
          "Permanently removes an admin. CAUTION: This will immediately invalidate any active JWT tokens for this admin.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Admin deleted"),
        @ApiResponse(responseCode = "404", description = "Admin not found")
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteAdmin(
      @Parameter(description = "The unique identifier of the admin to delete") @PathVariable
          String id) {
    adminService.deleteAdmin(id);
    return ResponseEntity.noContent().build();
  }
}
