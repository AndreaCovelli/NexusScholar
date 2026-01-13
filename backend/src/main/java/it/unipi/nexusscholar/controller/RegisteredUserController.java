package it.unipi.nexusscholar.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.RegisteredUserService;
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
 * REST controller for managing Registered Users.
 *
 * <p>Provides endpoints for registration, profile updates, retrieval, deletion, search, and
 * bookmark management.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description =
        "Operations related to registered users, including authentication context and search.")
public class RegisteredUserController {

  private final RegisteredUserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  /**
   * Registers a new user.
   *
   * @param createDTO The user creation payload.
   * @return The created user profile.
   */
  @Operation(
      summary = "Register a new User",
      description = "Creates a new user account with the provided details.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = RegisteredUserDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or duplicate username/email")
      })
  @PostMapping("/register")
  public ResponseEntity<RegisteredUserDTO> registerUser(
      @RequestBody RegisteredUserCreateDTO createDTO) {
    return ResponseEntity.ok(userService.registerUser(createDTO));
  }

  /**
   * Updates an existing user's profile.
   *
   * @param id The ID of the user to update.
   * @param updateDTO The updated data (username, email, password, full name).
   * @return The updated user profile.
   */
  @Operation(
      summary = "Update User Profile",
      description =
          "Updates email, full name, username, or password. Requires 'USER' or 'ADMIN' role.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Duplicate username/email or invalid data")
      })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<RegisteredUserDTO> updateUser(
      @Parameter(description = "ID of the user to update") @PathVariable String id,
      @RequestBody RegisteredUserUpdateDTO updateDTO) {
    return ResponseEntity.ok(userService.updateUser(id, updateDTO));
  }

  /**
   * Searches for users by their full name using a prefix search.
   *
   * @param name The prefix of the full name to search for.
   * @return A list of matching users.
   */
  @Operation(
      summary = "Search Users by Name",
      description =
          "Finds users whose full name starts with the provided string (using regex index).")
  @GetMapping("/search")
  public ResponseEntity<List<RegisteredUserDTO>> searchUsers(
      @Parameter(description = "Prefix of the full name") @RequestParam String name) {
    return ResponseEntity.ok(userService.searchUsersByFullName(name));
  }

  /**
   * Retrieves the current authenticated user's ID and username directly from the token.
   *
   * @param request The HTTP request containing the JWT token.
   * @return A map containing "id" and "username".
   */
  @Operation(
      summary = "Get Current User Info",
      description =
          "Extracts ID and Username directly from the JWT Token without querying the database.")
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, String>> getCurrentUserInfo(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);

    // Extract info from token
    String userId = jwtTokenProvider.getUserIdFromToken(token);
    String username = jwtTokenProvider.getUsernameFromToken(token);

    Map<String, String> userInfo = new HashMap<>();
    userInfo.put("id", userId);
    userInfo.put("username", username);

    return ResponseEntity.ok(userInfo);
  }

  /**
   * Retrieves a specific user by ID.
   *
   * @param id The user ID.
   * @return The user profile.
   */
  @Operation(
      summary = "Get User by ID",
      description = "Retrieves detailed information for a specific user.")
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<RegisteredUserDTO> getUserById(@PathVariable String id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  /**
   * Retrieves all users with pagination.
   *
   * @param pageable Pagination info.
   * @return A page of users.
   */
  @Operation(
      summary = "Get All Users",
      description = "Retrieves a paginated list of all users. Requires ADMIN role.")
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<RegisteredUserDTO>> getAllUsers(
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(userService.getAllUsers(pageable));
  }

  /**
   * Deletes a user by ID.
   *
   * @param id The user ID.
   * @return No content.
   */
  @Operation(
      summary = "Delete User",
      description = "Permanently removes a user from the system. Requires ADMIN role.")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Adds a bookmark for the current user.
   *
   * @param paperId The ID of the paper to bookmark.
   * @param request The HTTP request containing the JWT.
   * @return The updated user profile.
   */
  @Operation(
      summary = "Bookmark a Paper",
      description = "Adds a paper to the current user's bookmarks.")
  @PostMapping("/bookmarks/{paperId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<RegisteredUserDTO> addBookmark(
      @PathVariable String paperId, HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    String userId = jwtTokenProvider.getUserIdFromToken(token);
    return ResponseEntity.ok(userService.addBookmark(userId, paperId));
  }
}
