package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisteredUserDAO extends MongoRepository<RegisteredUser, String> {

    List<RegisteredUser> findByAffiliation(String affiliation);
}