package it.unipi.nexusscholar.controller;

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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class RegisteredUserController {

  private final RegisteredUserService userService;
  private final JwtTokenProvider jwtTokenProvider; // Injected to extract User ID from token

  // REGISTRATION (Public)
  @PostMapping("/register")
  public ResponseEntity<RegisteredUserDTO> registerUser(
      @RequestBody RegisteredUserCreateDTO createDTO) {
    return ResponseEntity.ok(userService.registerUser(createDTO));
  }

  // UPDATE (Only the user themselves or Admin)
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<RegisteredUserDTO> updateUser(
      @PathVariable String id, @RequestBody RegisteredUserUpdateDTO updateDTO) {
    return ResponseEntity.ok(userService.updateUser(id, updateDTO));
  }

  // GET BY ID
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<RegisteredUserDTO> getUserById(@PathVariable String id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  // LIST ALL (Only Admin should see all users)
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<RegisteredUserDTO>> getAllUsers(
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(userService.getAllUsers(pageable));
  }

  // DELETE
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Adds a bookmark for the current user. The user ID is extracted directly from the JWT token, so
   * the user doesn't need to provide it.
   */
  @PostMapping("/bookmarks/{paperId}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<RegisteredUserDTO> addBookmark(
      @PathVariable String paperId, HttpServletRequest request) {

    // 1. Resolve token from request header
    String token = jwtTokenProvider.resolveToken(request);

    // 2. Extract User ID from the token payload
    String userId = jwtTokenProvider.getUserIdFromToken(token);

    // 3. Delegate to service
    return ResponseEntity.ok(userService.addBookmark(userId, paperId));
  }
}
