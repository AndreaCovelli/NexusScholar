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
 *
 * <p>Handles core business logic including password hashing, uniqueness validation, and interaction
 * with the data access layer.
 */
@Service
@RequiredArgsConstructor
public class RegisteredUserServiceImpl implements RegisteredUserService {

  private final RegisteredUserDAO userDAO;
  private final PaperDAO paperDAO;
  private final PasswordEncoder passwordEncoder;

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
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setFullName(dto.getFullName());
    user.setCreatedAt(LocalDateTime.now());
    user.setBookmarkedPapers(new ArrayList<>());

    return mapToDTO(userDAO.save(user));
  }

  @Override
  public RegisteredUserDTO updateUser(String id, RegisteredUserUpdateDTO updateDTO) {
    RegisteredUser user =
        userDAO
            .findById(id)
            .orElseThrow(() -> new BusinessException("User not found with id: " + id));

    if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
      if (userDAO.existsByEmailAndIdNot(updateDTO.getEmail(), id)) {
        throw new IllegalArgumentException("Email is already taken by another user.");
      }
      user.setEmail(updateDTO.getEmail());
    }

    if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
      if (userDAO.existsByUsernameAndIdNot(updateDTO.getUsername(), id)) {
        throw new IllegalArgumentException("Username is already taken by another user.");
      }
      user.setUsername(updateDTO.getUsername());
    }

    if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
      user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
    }

    if (updateDTO.getFullName() != null) {
      user.setFullName(updateDTO.getFullName());
    }

    return mapToDTO(userDAO.save(user));
  }

  @Override
  public List<RegisteredUserDTO> searchUsersByFullName(String namePrefix) {
    if (namePrefix == null || namePrefix.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return userDAO.findByFullNameStartingWith(namePrefix).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public RegisteredUserDTO getUserById(String id) {
    return userDAO
        .findById(id)
        .map(this::mapToDTO)
        .orElseThrow(() -> new BusinessException("User not found with id: " + id));
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

  @Override
  public RegisteredUserDTO addBookmark(String userId, String paperId) {
    RegisteredUser user =
        userDAO.findById(userId).orElseThrow(() -> new BusinessException("User not found"));
    Paper paper =
        paperDAO
            .findById(paperId)
            .orElseThrow(() -> new BusinessException("Paper not found with id: " + paperId));

    if (user.getBookmarkedPapers() == null) {
      user.setBookmarkedPapers(new ArrayList<>());
    }

    boolean alreadyBookmarked =
        user.getBookmarkedPapers().stream().anyMatch(bp -> bp.getPaperId().equals(paperId));

    if (alreadyBookmarked) {
      throw new IllegalArgumentException("Paper is already bookmarked.");
    }

    BookmarkedPaper bookmark = new BookmarkedPaper();
    bookmark.setPaperId(paper.getId());
    bookmark.setTitle(paper.getTitle());
    bookmark.setSavedAt(LocalDateTime.now());

    user.getBookmarkedPapers().add(bookmark);
    return mapToDTO(userDAO.save(user));
  }

  private RegisteredUserDTO mapToDTO(RegisteredUser user) {
    RegisteredUserDTO dto = new RegisteredUserDTO();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setFullName(user.getFullName());

    if (user.getBookmarkedPapers() != null) {
      dto.setBookmarkedPapers(
          user.getBookmarkedPapers().stream()
              .map(bp -> new BookmarkedPaperDTO(bp.getPaperId(), bp.getTitle(), bp.getSavedAt()))
              .collect(Collectors.toList()));
    } else {
      dto.setBookmarkedPapers(Collections.emptyList());
    }
    return dto;
  }
}
