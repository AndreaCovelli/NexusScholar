package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Admin;
import java.util.Optional;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDAO extends MongoRepository<Admin, String> {

  Optional<Admin> findByUsername(String username);


  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

}
