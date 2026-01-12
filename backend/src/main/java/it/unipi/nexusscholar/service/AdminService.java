package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.AdminCreateDTO;
import it.unipi.nexusscholar.dto.mongo.AdminDTO;
import it.unipi.nexusscholar.dto.mongo.AdminUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing Administrator entities.
 * <p>
 * Defines the business logic for the lifecycle of administrative accounts,
 * including creation, updates, retrieval, and deletion. It acts as the bridge
 * between the controller layer and the persistence layer (DAO).
 * </p>
 */
public interface AdminService {

  /**
   * Creates and registers a new Administrator.
   * <p>
   * This method is responsible for validating the uniqueness of credentials
   * and securely hashing the password before storage.
   * </p>
   *
   * @param createDTO The DTO containing the initial admin data (username, email, raw password, permissions).
   * @return The created admin details as a {@link AdminDTO} (excluding the password).
   * @throws IllegalArgumentException If the username or email is already in use.
   */
  AdminDTO createAdmin(AdminCreateDTO createDTO);

  /**
   * Updates an existing Administrator's profile.
   * <p>
   * Allows modifying the email, password, or permission set.
   * Password updates are optional; if provided, the new password will be hashed.
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
   * @throws RuntimeException If no admin is found with the given ID.
   */
  AdminDTO getAdminById(String id);

  /**
   * Retrieves a paginated list of all Administrators in the system.
   * <p>
   * Useful for back-office management dashboards.
   * </p>
   *
   * @param pageable The pagination information (page number, size, sorting).
   * @return A page of {@link AdminDTO} objects.
   */
  Page<AdminDTO> getAllAdmins(Pageable pageable);

  /**
   * Permanently deletes an Administrator from the system.
   *
   * @param id The unique identifier of the admin to delete.
   * @throws RuntimeException If the admin does not exist.
   */
  void deleteAdmin(String id);
}