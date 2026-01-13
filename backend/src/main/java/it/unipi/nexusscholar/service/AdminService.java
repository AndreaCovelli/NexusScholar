package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Administrator entities.
 * <p>
 * Defines the business logic for the lifecycle of administrative accounts,
 * including creation, updates, retrieval, search, and deletion.
 * </p>
 */
public interface AdminService {

  /**
   * Creates and registers a new Administrator.
   *
   * @param createDTO The DTO containing the initial admin data.
   * @return The created admin details as a {@link AdminDTO}.
   * @throws IllegalArgumentException If the username or email is already in use.
   */
  AdminDTO createAdmin(AdminCreateDTO createDTO);

  /**
   * Updates an existing Administrator's profile.
   * <p>
   * Allows modifying the username, email, password, or permission set.
   * Checks uniqueness if username or email are changed.
   * </p>
   *
   * @param id        The unique identifier of the admin to update.
   * @param updateDTO The DTO containing the updated fields.
   * @return The updated admin details.
   * @throws RuntimeException If the admin is not found.
   */
  AdminDTO updateAdmin(String id, AdminUpdateDTO updateDTO);

  /**
   * Retrieves an Administrator by their unique ID.
   *
   * @param id The unique identifier.
   * @return The found admin details.
   */
  AdminDTO getAdminById(String id);

  /**
   * Retrieves a paginated list of all Administrators.
   *
   * @param pageable The pagination information.
   * @return A page of {@link AdminDTO} objects.
   */
  Page<AdminDTO> getAllAdmins(Pageable pageable);

  /**
   * Searches for administrators by username prefix.
   * <p>
   * Uses a regex-based search to find admins whose username starts with the query.
   * </p>
   *
   * @param usernamePrefix The starting characters of the username.
   * @return A list of matching AdminDTOs.
   */
  List<AdminDTO> searchAdmins(String usernamePrefix);

  /**
   * Permanently deletes an Administrator from the system.
   * <p>
   * <b>Note:</b> Deleting the admin will automatically invalidate their active JWT tokens
   * due to the database existence check in the security filter.
   * </p>
   *
   * @param id The unique identifier of the admin to delete.
   */
  void deleteAdmin(String id);
}