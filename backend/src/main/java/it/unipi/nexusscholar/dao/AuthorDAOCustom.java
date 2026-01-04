package it.unipi.nexusscholar.dao;

import java.util.List;

import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import org.bson.Document;

public interface AuthorDAOCustom {

  /**
   * Find authors who have published in more than “minPublication” distinct keywords/topics within a
   * single year , identifying interdisciplinary researchers.
   *
   * @param minPublications Number of minimum publications
   * @return
   */
  List<Document> getProlificAuthors(int minPublications);
}
