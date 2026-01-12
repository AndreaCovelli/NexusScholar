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
import it.unipi.nexusscholar.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Administrator resources.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting administrator accounts.
 * Access to most endpoints is restricted to users with the 'ADMIN' role.
 * </p>
 */
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "APIs for managing administrator accounts, permissions, and lifecycle.")
public class AdminController {

  private final AdminService adminService;

  /**
   * Creates a new Administrator.
   *
   * @param createDTO The DTO containing username, email, password, and permissions.
   * @return The created Admin details.
   */
  @Operation(
          summary = "Create a new Administrator",
          description = "Registers a new admin account with specific permissions. Checks for duplicate username or email."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Admin created successfully",
                  content = @Content(schema = @Schema(implementation = AdminDTO.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data or Username/Email already exists")
  })
  @PostMapping
  public ResponseEntity<AdminDTO> createAdmin(@RequestBody AdminCreateDTO createDTO) {
    return ResponseEntity.ok(adminService.createAdmin(createDTO));
  }

  /**
   * Updates an existing Administrator's profile.
   *
   * @param id        The unique ID of the admin.
   * @param updateDTO The updated data (email, password, permissions).
   * @return The updated Admin details.
   */
  @Operation(
          summary = "Update an Administrator",
          description = "Updates email, password, or permissions for an existing admin. Requires 'ADMIN' role."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Admin updated successfully",
                  content = @Content(schema = @Schema(implementation = AdminDTO.class))),
          @ApiResponse(responseCode = "404", description = "Admin not found"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
          @ApiResponse(responseCode = "400", description = "Email already in use by another admin")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AdminDTO> updateAdmin(
          @Parameter(description = "The unique identifier of the admin to update") @PathVariable String id,
          @RequestBody AdminUpdateDTO updateDTO) {
    return ResponseEntity.ok(adminService.updateAdmin(id, updateDTO));
  }

  /**
   * Retrieves an Administrator by their ID.
   *
   * @param id The unique ID of the admin.
   * @return The requested Admin details.
   */
  @Operation(
          summary = "Get Admin by ID",
          description = "Retrieves detailed information about a specific administrator. Requires Authentication."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Admin found",
                  content = @Content(schema = @Schema(implementation = AdminDTO.class))),
          @ApiResponse(responseCode = "404", description = "Admin not found"),
          @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
  })
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AdminDTO> getAdminById(
          @Parameter(description = "The unique identifier of the admin") @PathVariable String id) {
    return ResponseEntity.ok(adminService.getAdminById(id));
  }

  /**
   * Retrieves a paginated list of all Administrators.
   *
   * @param pageable Pagination info (page, size, sort).
   * @return A page of AdminDTOs.
   */
  @Operation(
          summary = "List all Administrators",
          description = "Retrieves a paginated list of all registered admins. Requires 'ADMIN' role."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
  })
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<AdminDTO>> getAllAdmins(
          @Parameter(description = "Pagination parameters (page, size, sort)", hidden = true)
          @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(adminService.getAllAdmins(pageable));
  }

  /**
   * Permanently deletes an Administrator.
   *
   * @param id The unique ID of the admin to delete.
   * @return No Content (204) on success.
   */
  @Operation(
          summary = "Delete an Administrator",
          description = "Permanently removes an admin account from the system. Requires 'ADMIN' role."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Admin deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Admin not found"),
          @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteAdmin(
          @Parameter(description = "The unique identifier of the admin to delete") @PathVariable String id) {
    adminService.deleteAdmin(id);
    return ResponseEntity.noContent().build();
  }
}