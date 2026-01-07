package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredUserDAO extends MongoRepository<RegisteredUser, String> {

  List<RegisteredUser> findByAffiliation(String affiliation);
}
