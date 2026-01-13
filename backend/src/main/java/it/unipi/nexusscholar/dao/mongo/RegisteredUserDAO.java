package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link RegisteredUser} entities in MongoDB.
 *
 * <p>Extends {@link MongoRepository} to provide standard CRUD operations and custom finder methods
 * optimized for the specific indices defined in the database.
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
   *
   * <p>This is critical during profile updates to ensure the new email does not conflict with
   * another existing user.
   *
   * @param email The email to check.
   * @param id The ID of the user performing the update (to be excluded from the check).
   * @return {@code true} if the email is used by another user; {@code false} otherwise.
   */
  boolean existsByEmailAndIdNot(String email, String id);

  /**
   * Checks if a username is used by any user <i>other than</i> the one with the specified ID.
   *
   * <p>Used during profile updates to ensure unique usernames across the system.
   *
   * @param username The username to check.
   * @param id The ID of the user performing the update.
   * @return {@code true} if the username is used by another user; {@code false} otherwise.
   */
  boolean existsByUsernameAndIdNot(String username, String id);

  /**
   * Finds users whose full name starts with the specified prefix.
   *
   * <p>This method leverages the MongoDB index on the {@code full_name} field using a regex query
   * anchor (e.g., {@code /^Prefix/}).
   *
   * @param prefix The prefix string to search for.
   * @return A list of users matching the criteria.
   */
  List<RegisteredUser> findByFullNameStartingWith(String prefix);
}
