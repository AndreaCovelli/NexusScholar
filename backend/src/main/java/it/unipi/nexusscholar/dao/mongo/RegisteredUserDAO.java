package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link RegisteredUser} entities in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard CRUD operations and custom finder methods.
 * </p>
 */
@Repository
public interface RegisteredUserDAO extends MongoRepository<RegisteredUser, String> {

  /**
   * Finds a registered user by their username.
   *
   * @param username The unique username to search for.
   * @return An {@link Optional} containing the user if found, or empty otherwise.
   */
  Optional<RegisteredUser> findByUsername(String username);

  /**
   * Checks if a user exists with the given username.
   *
   * @param username The username to validate.
   * @return {@code true} if the username is already taken; {@code false} otherwise.
   */
  boolean existsByUsername(String username);

  /**
   * Checks if a user exists with the given email address.
   *
   * @param email The email to validate.
   * @return {@code true} if the email is already in use; {@code false} otherwise.
   */
  boolean existsByEmail(String email);

  /**
   * Checks if an email is used by any user <i>other than</i> the one with the specified ID.
   * <p>
   * Useful during profile updates to ensure the new email doesn't conflict with another user.
   * </p>
   *
   * @param email The email to check.
   * @param id    The ID of the current user (to be excluded from the check).
   * @return {@code true} if the email is taken by another user.
   */
  boolean existsByEmailAndIdNot(String email, String id);
}