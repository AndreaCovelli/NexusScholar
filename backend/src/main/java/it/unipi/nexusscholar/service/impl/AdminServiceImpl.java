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
 * <p>
 * This class encapsulates the business logic for managing Administrator accounts.
 * It coordinates interactions between the data access layer (DAO) and security components
 * (Password Encoder), ensuring that sensitive operations like password storage are handled securely.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final AdminDAO adminDAO;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates a new Administrator account.
   * <p>
   * <b>Business Logic:</b>
   * <ol>
   * <li>Checks if the provided username or email is already in use (Fail-fast).</li>
   * <li>Hashes the raw password using BCrypt (via {@link PasswordEncoder}).</li>
   * <li>Sets the initial creation timestamp.</li>
   * <li>Persists the entity and returns the sanitized DTO.</li>
   * </ol>
   * </p>
   *
   * @param dto The data transfer object containing the new admin's details.
   * @return The created admin as a DTO.
   * @throws IllegalArgumentException If the username or email already exists in the database.
   */
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
    // Encrypt password before saving
    admin.setPassword(passwordEncoder.encode(dto.getPassword()));
    admin.setPermissions(dto.getPermissions());
    admin.setCreatedAt(LocalDateTime.now());

    Admin savedAdmin = adminDAO.save(admin);

    return mapToDTO(savedAdmin);
  }

  /**
   * Updates an existing Administrator's profile.
   * <p>
   * <b>Logic:</b>
   * <ul>
   * <li><b>Email Validation:</b> Checks if the email is being changed. If so, ensures the new email
   * does not belong to another admin.</li>
   * <li><b>Password Logic:</b> Checks if the new password field is not null and not blank.
   * Only then does it re-hash the new password and update the field.
   * Otherwise, the old password remains unchanged.</li>
   * <li><b>Permissions:</b> Updates the list of permissions directly.</li>
   * </ul>
   * </p>
   *
   * @param id  The ID of the admin to update.
   * @param dto The DTO containing updated fields.
   * @return The updated admin details.
   * @throws RuntimeException If the admin is not found.
   * @throws IllegalArgumentException If the new email is already in use.
   */
  @Override
  public AdminDTO updateAdmin(String id, AdminUpdateDTO dto) {
    Admin admin =
            adminDAO.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));

    // 1. Email Uniqueness Check
    // We only check DB if the email is actually changing
    if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(admin.getEmail())) {
      if (adminDAO.existsByEmail(dto.getEmail())) {
        throw new IllegalArgumentException("Email already in use by another admin");
      }
      admin.setEmail(dto.getEmail());
    }

    // 2. Update Permissions
    if (dto.getPermissions() != null) {
      admin.setPermissions(dto.getPermissions());
    }

    // 3. Update Password only if a new one is provided
    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      admin.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    return mapToDTO(adminDAO.save(admin));
  }

  /**
   * Retrieves an Admin by ID.
   *
   * @param id The admin ID.
   * @return The admin DTO.
   * @throws RuntimeException If the admin is not found.
   */
  @Override
  public AdminDTO getAdminById(String id) {
    Admin admin =
            adminDAO.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
    return mapToDTO(admin);
  }

  /**
   * Retrieves all admins with pagination.
   *
   * @param pageable The pagination information.
   * @return A page of AdminDTOs.
   */
  @Override
  public Page<AdminDTO> getAllAdmins(Pageable pageable) {
    // Stream through results and map each entity to DTO
    return adminDAO.findAll(pageable).map(this::mapToDTO);
  }

  /**
   * Deletes an admin permanently.
   *
   * @param id The admin ID.
   * @throws RuntimeException If the admin does not exist.
   */
  @Override
  public void deleteAdmin(String id) {
    if (!adminDAO.existsById(id)) {
      throw new RuntimeException("Admin not found");
    }
    adminDAO.deleteById(id);
  }

  /**
   * Helper method to map the internal {@link Admin} entity to the public {@link AdminDTO}.
   * <p>
   * This method also handles the conversion of the Permissions list from the internal
   * model Enum to the DTO Enum to decouple the API from the database model.
   * </p>
   *
   * @param admin The entity to convert.
   * @return The resulting DTO.
   */
  private AdminDTO mapToDTO(Admin admin) {
    AdminDTO dto = new AdminDTO();
    dto.setId(admin.getId());
    dto.setUsername(admin.getUsername());
    dto.setEmail(admin.getEmail());
    dto.setCreatedAt(admin.getCreatedAt());

    // Safely map Permissions
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