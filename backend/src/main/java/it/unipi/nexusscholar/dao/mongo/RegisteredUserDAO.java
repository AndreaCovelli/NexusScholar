package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredUserDAO extends MongoRepository<RegisteredUser, String> {

  // Standard find methods
  Optional<RegisteredUser> findByUsername(String username);

  // Checks for existence (used during Registration)
  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, String id);
}
