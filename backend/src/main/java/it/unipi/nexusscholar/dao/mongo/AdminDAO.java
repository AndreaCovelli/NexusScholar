package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Admin;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) for managing {@link Admin} entities.
 * <p>
 * Extends {@link MongoRepository} to provide standard CRUD operations
 * and custom query methods for the "admins" collection in MongoDB.
 * </p>
 */
@Repository
public interface AdminDAO extends MongoRepository<Admin, String> {

  /**
   * Retrieves an Admin by their unique username.
   *
   * @param username The username to search for.
   * @return An {@link Optional} containing the Admin if found, or empty otherwise.
   */
  Optional<Admin> findByUsername(String username);

  /**
   * Checks if an Admin with the specified username already exists.
   * <p>
   * Useful for validation during the creation of new administrators.
   * </p>
   *
   * @param username The username to check.
   * @return {@code true} if the username exists, {@code false} otherwise.
   */
  boolean existsByUsername(String username);

  /**
   * Checks if an Admin with the specified email address already exists.
   * <p>
   * Useful for validation to ensure unique email addresses across the system.
   * </p>
   *
   * @param email The email address to check.
   * @return {@code true} if the email is already in use, {@code false} otherwise.
   */
  boolean existsByEmail(String email);
}