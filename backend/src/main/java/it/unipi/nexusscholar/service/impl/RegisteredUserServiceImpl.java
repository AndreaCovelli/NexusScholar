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

@Service
@RequiredArgsConstructor
public class RegisteredUserServiceImpl implements RegisteredUserService {

  private final RegisteredUserDAO userDAO;
  private final PaperDAO paperDAO;
  private final PasswordEncoder passwordEncoder;

  @Override
  public RegisteredUserDTO registerUser(RegisteredUserCreateDTO dto) {
    // Validation: Check if username or email already exists
    if (userDAO.existsByUsername(dto.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (userDAO.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    // Create Entity
    RegisteredUser user = new RegisteredUser();
    user.setUsername(dto.getUsername());
    user.setEmail(dto.getEmail());
    user.setFullName(dto.getFullName());
    user.setCreatedAt(LocalDateTime.now());
    // Hash password
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    // Initialize empty bookmarks list
    user.setBookmarkedPapers(new ArrayList<>());

    return mapToDTO(userDAO.save(user));
  }

  @Override
  public RegisteredUserDTO updateUser(String id, RegisteredUserUpdateDTO dto) {
    RegisteredUser user =
        userDAO.findById(id).orElseThrow(() -> new BusinessException("User not found"));

    // 1. Email Update Check
    if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
      if (userDAO.existsByEmailAndIdNot(dto.getEmail(), id)) {
        throw new IllegalArgumentException("Email already in use by another user");
      }
      user.setEmail(dto.getEmail());
    }

    // 2. Profile Info Update
    if (dto.getFullName() != null) {
      user.setFullName(dto.getFullName());
    }

    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    // 3. Bookmarks Update (STRICT LOGIC like addBookmark)
    if (dto.getBookmarkedPapers() != null) {
      List<BookmarkedPaper> validatedBookmarks = new ArrayList<>();

      for (BookmarkedPaperDTO bookmarkDto : dto.getBookmarkedPapers()) {
        // LOGIC: Don't trust the DTO title. Fetch the real Paper from DB.
        Paper paper =
            paperDAO
                .findById(bookmarkDto.getPaperId())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Paper not found with ID: " + bookmarkDto.getPaperId()));

        BookmarkedPaper newBookmark = new BookmarkedPaper();
        newBookmark.setPaperId(paper.getId());
        newBookmark.setTitle(paper.getTitle()); // <--- This ensures consistency with DB

        // Keep the date provided or set to now
        newBookmark.setSavedAt(
            bookmarkDto.getSavedAt() != null ? bookmarkDto.getSavedAt() : LocalDateTime.now());

        validatedBookmarks.add(newBookmark);
      }

      // Replace the old list with the validated one
      user.setBookmarkedPapers(validatedBookmarks);
    }

    return mapToDTO(userDAO.save(user));
  }

  @Override
  public RegisteredUserDTO getUserById(String id) {
    return userDAO
        .findById(id)
        .map(this::mapToDTO)
        .orElseThrow(() -> new BusinessException("User not found"));
  }

  @Override
  public Page<RegisteredUserDTO> getAllUsers(Pageable pageable) {
    return userDAO.findAll(pageable).map(this::mapToDTO);
  }

  @Override
  public void deleteUser(String id) {
    if (!userDAO.existsById(id)) throw new BusinessException("User not found");
    userDAO.deleteById(id);
  }

  @Override
  public RegisteredUserDTO addBookmark(String userId, String paperId) {
    // 1. Fetch the user
    RegisteredUser user =
        userDAO.findById(userId).orElseThrow(() -> new BusinessException("User not found"));

    // 2. Fetch the paper to ensure it exists and to get the title
    Paper paper =
        paperDAO
            .findById(paperId)
            .orElseThrow(() -> new BusinessException("Paper not found with ID: " + paperId));

    // 3. Initialize the list if it's null
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

  // --- Helper Mapping ---
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
