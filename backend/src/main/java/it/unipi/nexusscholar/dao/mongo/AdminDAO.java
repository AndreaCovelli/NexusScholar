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

  // Optional<Admin> findByEmail(String email);

  // Utile per validazioni nel service
  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  // Esempio di ricerca paginata (simile a PaperDAO)
  // Page<Admin> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
