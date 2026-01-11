package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegisteredUserDAO extends MongoRepository<RegisteredUser, String> {
    Optional<RegisteredUser> findByUsername(String username);
    //Optional<RegisteredUser> findByEmail(String email);

    // Useful for validation in service layer
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}