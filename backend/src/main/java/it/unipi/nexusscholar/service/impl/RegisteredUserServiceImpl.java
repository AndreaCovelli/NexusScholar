package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.mongo.PaperDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import it.unipi.nexusscholar.dto.mongo.*;
import it.unipi.nexusscholar.model.mongo.BookmarkedPaper;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import it.unipi.nexusscholar.service.RegisteredUserService;
import it.unipi.nexusscholar.service.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link RegisteredUserService}.
 * <p>
 * Handles the core business logic for user management, including password hashing,
 * duplicate checks, and managing the relationship between users and papers (bookmarks).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RegisteredUserServiceImpl implements RegisteredUserService {

  private final RegisteredUserDAO userDAO;
  private final PaperDAO paperDAO;
  private final PasswordEncoder passwordEncoder;

  /**
   * Registers a new user.
   * <p>
   * <b>Logic:</b>
   * <ol>
   * <li>Checks if the username or email already exists.</li>
   * <li>Encodes the password using BCrypt.</li>
   * <li>Initializes the bookmark list.</li>
   * <li>Saves the entity to MongoDB.</li>
   * </ol>
   * </p>
   */
  @Override
  public RegisteredUserDTO registerUser(RegisteredUserCreateDTO dto) {
    if (userDAO.existsByUsername(dto.getUsername())) {
      throw new IllegalArgumentException("Username already exists.");
    }
    if (userDAO.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already in use.");
    }

    RegisteredUser user = new RegisteredUser();
    user.setUsername(dto.getUsername());
    user.setEmail(dto.getEmail());
    // Securely hash the password before storage
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setFullName(dto.getFullName());
    user.setCreatedAt(LocalDateTime.now());
    user.setBookmarkedPapers(new ArrayList<>());

    RegisteredUser savedUser = userDAO.save(user);
    return mapToDTO(savedUser);
  }

  /**
   * Updates an existing user.
   * <p>
   * Ensures that if the email is being changed, it does not conflict with another existing user.
   * </p>
   */
  @Override
  public RegisteredUserDTO updateUser(String id, RegisteredUserUpdateDTO updateDTO) {
    RegisteredUser user =
            userDAO
                    .findById(id)
                    .orElseThrow(() -> new BusinessException("User not found with id: " + id));

    // Validate email uniqueness if it's being changed
    if (userDAO.existsByEmailAndIdNot(updateDTO.getEmail(), id)) {
      throw new IllegalArgumentException("Email is already taken by another user.");
    }

    user.setEmail(updateDTO.getEmail());
    user.setFullName(updateDTO.getFullName());
    // Note: Password update is usually handled in a separate, dedicated method for security

    return mapToDTO(userDAO.save(user));
  }

  @Override
  public RegisteredUserDTO getUserById(String id) {
    RegisteredUser user =
            userDAO
                    .findById(id)
                    .orElseThrow(() -> new BusinessException("User not found with id: " + id));
    return mapToDTO(user);
  }

  @Override
  public Page<RegisteredUserDTO> getAllUsers(Pageable pageable) {
    return userDAO.findAll(pageable).map(this::mapToDTO);
  }

  @Override
  public void deleteUser(String id) {
    if (!userDAO.existsById(id)) {
      throw new BusinessException("User not found with id: " + id);
    }
    userDAO.deleteById(id);
  }

  /**
   * Adds a bookmark for a specific paper.
   * <p>
   * <b>Logic:</b>
   * <ol>
   * <li>Verifies the user and the paper exist.</li>
   * <li>Checks if the paper is <i>already</i> in the user's bookmark list to prevent duplicates.</li>
   * <li>Creates a {@link BookmarkedPaper} entry with the current timestamp.</li>
   * <li>Updates the user document.</li>
   * </ol>
   * </p>
   */
  @Override
  public RegisteredUserDTO addBookmark(String userId, String paperId) {
    // 1. Retrieve User
    RegisteredUser user =
            userDAO
                    .findById(userId)
                    .orElseThrow(() -> new BusinessException("User not found"));

    // 2. Retrieve Paper
    Paper paper =
            paperDAO
                    .findById(paperId)
                    .orElseThrow(() -> new BusinessException("Paper not found with id: " + paperId));

    // 3. Initialize list if null (defensive coding)
    if (user.getBookmarkedPapers() == null) {
      user.setBookmarkedPapers(new ArrayList<>());
    }

    // 4. Check for duplicates
    boolean alreadyBookmarked =
            user.getBookmarkedPapers().stream().anyMatch(bp -> bp.getPaperId().equals(paperId));

    if (alreadyBookmarked) {
      throw new IllegalArgumentException("Paper is already bookmarked.");
    }

    // 5. Create the bookmark entry
    BookmarkedPaper bookmark = new BookmarkedPaper();
    bookmark.setPaperId(paper.getId());
    bookmark.setTitle(paper.getTitle());
    bookmark.setSavedAt(LocalDateTime.now());

    // 6. Add and save
    user.getBookmarkedPapers().add(bookmark);

    return mapToDTO(userDAO.save(user));
  }

  /**
   * Utility method to map a {@link RegisteredUser} entity to a {@link RegisteredUserDTO}.
   *
   * @param user The user entity.
   * @return The mapped DTO.
   */
  private RegisteredUserDTO mapToDTO(RegisteredUser user) {
    RegisteredUserDTO dto = new RegisteredUserDTO();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setFullName(user.getFullName());

    if (user.getBookmarkedPapers() != null) {
      List<BookmarkedPaperDTO> bookmarkDTOs =
              user.getBookmarkedPapers().stream()
                      .map(bp -> new BookmarkedPaperDTO(bp.getPaperId(), bp.getTitle(), bp.getSavedAt()))
                      .collect(Collectors.toList());
      dto.setBookmarkedPapers(bookmarkDTOs);
    } else {
      dto.setBookmarkedPapers(Collections.emptyList());
    }

    return dto;
  }
}