package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.RegisteredUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Registered Users.
 * <p>
 * Provides endpoints for registration, profile updates, retrieval, deletion,
 * and bookmark management.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "CRUD operations and Bookmark management for users")
public class RegisteredUserController {

  private final RegisteredUserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  /**
   * Registers a new user.
   * <p>
   * This endpoint is public and does not require authentication.
   * </p>
   *
   * @param createDTO The user registration details.
   * @return The created user object.
   */
  @Operation(summary = "Register a new User", description = "Creates a new registered user account. Public endpoint.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User registered successfully"),
          @ApiResponse(responseCode = "400", description = "Username or Email already exists")
  })
  @PostMapping("/register")
  public ResponseEntity<RegisteredUserDTO> registerUser(
          @RequestBody RegisteredUserCreateDTO createDTO) {
    return ResponseEntity.ok(userService.registerUser(createDTO));
  }

  /**
   * Updates an existing user's profile.
   *
   * @param id        The ID of the user to update.
   * @param updateDTO The updated details.
   * @return The updated user object.
   */
  @Operation(summary = "Update User Profile", description = "Updates email or full name. Requires 'USER' or 'ADMIN' role.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User updated successfully"),
          @ApiResponse(responseCode = "404", description = "User not found"),
          @ApiResponse(responseCode = "400", description = "Email already taken by another user")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<RegisteredUserDTO> updateUser(
          @Parameter(description = "MongoDB ID of the user") @PathVariable String id,
          @RequestBody RegisteredUserUpdateDTO updateDTO) {
    return ResponseEntity.ok(userService.updateUser(id, updateDTO));
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param id The user ID.
   * @return The user details.
   */
  @Operation(summary = "Get User by ID", description = "Retrieves user details. Requires valid authentication.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User found"),
          @ApiResponse(responseCode = "404", description = "User not found")
  })
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<RegisteredUserDTO> getUserById(
          @Parameter(description = "MongoDB ID of the user") @PathVariable String id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  /**
   * Retrieves all registered users (Paginated).
   * <p>
   * This endpoint is restricted to Administrators only.
   * </p>
   *
   * @param pageable Pagination info (page, size, sort).
   * @return A page of users.
   */
  @Operation(summary = "List all Users", description = "Retrieves a paginated list of all users. Restricted to 'ADMIN'.")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<RegisteredUserDTO>> getAllUsers(
          @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(userService.getAllUsers(pageable));
  }

  /**
   * Deletes a user permanently.
   * <p>
   * This endpoint is restricted to Administrators only.
   * </p>
   *
   * @param id The ID of the user to delete.
   * @return No content.
   */
  @Operation(summary = "Delete User", description = "Permanently removes a user. Restricted to 'ADMIN'.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "User deleted successfully"),
          @ApiResponse(responseCode = "404", description = "User not found")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(
          @Parameter(description = "MongoDB ID of the user") @PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Adds a bookmark for the currently authenticated user.
   * <p>
   * The user ID is automatically extracted from the JWT token in the request header,
   * ensuring users can only add bookmarks to their own profile.
   * </p>
   *
   * @param paperId The ID of the paper to bookmark.
   * @param request The HTTP request containing the JWT token.
   * @return The updated user profile.
   */
  @Operation(
          summary = "Bookmark a Paper",
          description = "Adds a paper to the current user's bookmarks. The User ID is inferred from the JWT token."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Bookmark added successfully"),
          @ApiResponse(responseCode = "400", description = "Paper already bookmarked or invalid ID"),
          @ApiResponse(responseCode = "404", description = "Paper not found")
  })
  @PostMapping("/bookmarks/{paperId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<RegisteredUserDTO> addBookmark(
          @Parameter(description = "ID of the paper to save") @PathVariable String paperId,
          HttpServletRequest request) {

    // 1. Resolve token from request header
    String token = jwtTokenProvider.resolveToken(request);

    // 2. Extract User ID from the token payload
    String userId = jwtTokenProvider.getUserIdFromToken(token);

    return ResponseEntity.ok(userService.addBookmark(userId, paperId));
  }
}