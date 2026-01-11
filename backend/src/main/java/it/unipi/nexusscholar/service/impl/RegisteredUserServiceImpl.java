package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import it.unipi.nexusscholar.dto.mongo.*;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import it.unipi.nexusscholar.service.RegisteredUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegisteredUserServiceImpl implements RegisteredUserService {

    private final RegisteredUserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisteredUserDTO registerUser(RegisteredUserCreateDTO dto) {
        // Validation
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
        RegisteredUser user = userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());

        // Update password if provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return mapToDTO(userDAO.save(user));
    }

    @Override
    public RegisteredUserDTO getUserById(String id) {
        return userDAO.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Page<RegisteredUserDTO> getAllUsers(Pageable pageable) {
        return userDAO.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public void deleteUser(String id) {
        if (!userDAO.existsById(id)) throw new RuntimeException("User not found");
        userDAO.deleteById(id);
    }

    // --- Helper Mapping ---
    private RegisteredUserDTO mapToDTO(RegisteredUser user) {
        RegisteredUserDTO dto = new RegisteredUserDTO();
        // Inherited fields from UserDTO
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());

        // Specific fields
        dto.setFullName(user.getFullName());

        // Map Bookmarks
        // Assuming RegisteredUser entity has a list of BookmarkedPaper (inner entity or POJO)
        if (user.getBookmarkedPapers() != null) {
            List<BookmarkedPaperDTO> bookmarkDTOs = user.getBookmarkedPapers().stream()
                    .map(bp -> new BookmarkedPaperDTO(
                            bp.getPaperId(),
                            bp.getTitle(),
                            bp.getSavedAt()
                    ))
                    .collect(Collectors.toList());
            dto.setBookmarkedPapers(bookmarkDTOs);
        } else {
            dto.setBookmarkedPapers(Collections.emptyList());
        }

        return dto;
    }
}