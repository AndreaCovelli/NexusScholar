package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import it.unipi.nexusscholar.dto.mongo.PermissionDTO;
import it.unipi.nexusscholar.model.mongo.Admin;
import it.unipi.nexusscholar.service.AdminService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link AdminService} interface.
 *
 * <p>Handles business logic for Admin management, including unique constraint checks for username
 * and email updates.
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final AdminDAO adminDAO;
  private final PasswordEncoder passwordEncoder;

  @Override
  public AdminDTO createAdmin(AdminCreateDTO dto) {
    if (adminDAO.existsByUsername(dto.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (adminDAO.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already in use");
    }

    Admin admin = new Admin();
    admin.setUsername(dto.getUsername());
    admin.setEmail(dto.getEmail());
    admin.setPassword(passwordEncoder.encode(dto.getPassword()));
    admin.setPermissions(dto.getPermissions());
    admin.setCreatedAt(LocalDateTime.now());

    Admin savedAdmin = adminDAO.save(admin);
    return mapToDTO(savedAdmin);
  }

  @Override
  public AdminDTO updateAdmin(String id, AdminUpdateDTO dto) {
    Admin admin = adminDAO.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

    // 1. Email Uniqueness Check
    if (dto.getEmail() != null
        && !dto.getEmail().isBlank()
        && !dto.getEmail().equals(admin.getEmail())) {
      if (adminDAO.existsByEmail(dto.getEmail())) {
        throw new IllegalArgumentException("Email already in use by another admin");
      }
      admin.setEmail(dto.getEmail());
    }

    // 2. Username Uniqueness Check
    if (dto.getUsername() != null
        && !dto.getUsername().isBlank()
        && !dto.getUsername().equals(admin.getUsername())) {
      if (adminDAO.existsByUsername(dto.getUsername())) {
        throw new IllegalArgumentException("Username already exists");
      }
      admin.setUsername(dto.getUsername());
    }

    // 3. Update Permissions
    if (dto.getPermissions() != null) {
      admin.setPermissions(dto.getPermissions());
    }

    // 4. Update Password only if a new one is provided
    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      admin.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    return mapToDTO(adminDAO.save(admin));
  }

  @Override
  public AdminDTO getAdminById(String id) {
    Admin admin = adminDAO.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
    return mapToDTO(admin);
  }

  @Override
  public Page<AdminDTO> getAllAdmins(Pageable pageable) {
    return adminDAO.findAll(pageable).map(this::mapToDTO);
  }

  @Override
  public List<AdminDTO> searchAdmins(String usernamePrefix) {
    // Uses the custom DAO method that leverages the { username: 1 } index via regex
    return adminDAO.findByUsernameStartingWith(usernamePrefix).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteAdmin(String id) {
    if (!adminDAO.existsById(id)) {
      throw new RuntimeException("Admin not found");
    }
    // Deleting the entity effectively invalidates any active JWTs
    // because JwtTokenProvider.validateToken() checks for DB existence.
    adminDAO.deleteById(id);
  }

  private AdminDTO mapToDTO(Admin admin) {
    AdminDTO dto = new AdminDTO();
    dto.setId(admin.getId());
    dto.setUsername(admin.getUsername());
    dto.setEmail(admin.getEmail());
    dto.setCreatedAt(admin.getCreatedAt());

    if (admin.getPermissions() != null) {
      List<PermissionDTO> permDTOs =
          admin.getPermissions().stream()
              .map(p -> PermissionDTO.valueOf(p.name()))
              .collect(Collectors.toList());
      dto.setPermissions(permDTOs);
    } else {
      dto.setPermissions(Collections.emptyList());
    }

    return dto;
  }
}
