package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.model.mongo.Admin;
import it.unipi.nexusscholar.model.mongo.Permission;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDAO extends MongoRepository<Admin, String> {

  List<Admin> findByPermissionsContaining(Permission permission);
}
