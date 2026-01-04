package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.model.mongo.Paper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.bson.Document;
import java.util.Optional;
@Repository
public interface PaperDAO extends MongoRepository<Paper, String>, PaperDAOCustom {

}