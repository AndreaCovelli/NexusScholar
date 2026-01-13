package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Admin;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) for managing {@link Admin} entities.
 *
 * <p>Extends {@link MongoRepository} to provide standard CRUD operations and custom query methods
 * for the "admins" collection in MongoDB.
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
   * Finds admins whose username starts with the given prefix.
   *
   * <p>This method translates to a MongoDB regex query: <code>{ username: /^prefix/ }</code>. It
   * leverages the index <code>{ username: 1 }</code> for performance.
   *
   * @param prefix The prefix of the username.
   * @return A list of matching Admins.
   */
  List<Admin> findByUsernameStartingWith(String prefix);

  /**
   * Checks if an Admin with the specified username already exists.
   *
   * @param username The username to check.
   * @return {@code true} if the username exists, {@code false} otherwise.
   */
  boolean existsByUsername(String username);

  /**
   * Checks if an Admin with the specified email address already exists.
   *
   * @param email The email address to check.
   * @return {@code true} if the email is already in use, {@code false} otherwise.
   */
  boolean existsByEmail(String email);
}
