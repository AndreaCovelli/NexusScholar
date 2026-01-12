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

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final AdminDAO adminDAO;
  private final PasswordEncoder passwordEncoder;

  @Override
  public AdminDTO createAdmin(AdminCreateDTO dto) {
    // Validate if username or email already exists
    if (adminDAO.existsByUsername(dto.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (adminDAO.existsByEmail(dto.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    Admin admin = new Admin();
    admin.setUsername(dto.getUsername());
    admin.setEmail(dto.getEmail());
    admin.setCreatedAt(LocalDateTime.now());
    admin.setPermissions(dto.getPermissions());

    // Hash the password before saving
    admin.setPassword(passwordEncoder.encode(dto.getPassword()));

    return mapToDTO(adminDAO.save(admin));
  }

  @Override
  public AdminDTO updateAdmin(String id, AdminUpdateDTO dto) {
    Admin admin = adminDAO.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

    if (dto.getEmail() != null) admin.setEmail(dto.getEmail());
    if (dto.getPermissions() != null) admin.setPermissions(dto.getPermissions());

    // Update password only if provided, and hash it
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
    // Fetch paginated results and map them to DTOs
    return adminDAO.findAll(pageable).map(this::mapToDTO);
  }

  @Override
  public void deleteAdmin(String id) {
    if (!adminDAO.existsById(id)) {
      throw new RuntimeException("Admin not found");
    }
    adminDAO.deleteById(id);
  }

  // --- Helper Method for Mapping ---
  private AdminDTO mapToDTO(Admin admin) {
    AdminDTO dto = new AdminDTO();
    dto.setId(admin.getId());
    dto.setUsername(admin.getUsername());
    dto.setEmail(admin.getEmail());
    dto.setCreatedAt(admin.getCreatedAt());

    // Map Permissions (Enum -> DTO) safely
    List<PermissionDTO> permDTOs =
        admin.getPermissions() != null
            ? admin.getPermissions().stream()
                .map(p -> PermissionDTO.valueOf(p.name()))
                .collect(Collectors.toList())
            : Collections.emptyList();

    dto.setPermissions(permDTOs);
    return dto;
  }
}
