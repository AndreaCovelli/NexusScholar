package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.dao.AuthorAnalysisDAO;
import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the {@link AuthorAnalysisDAO} interface.
 *
 * <p>uses {@link MongoTemplate} to construct and execute aggregation pipelines for analytical tasks
 * regarding authors.
 */
@Repository
@RequiredArgsConstructor
public class AuthorAnalysisDAOImpl implements AuthorAnalysisDAO {

  private final MongoTemplate mongoTemplate;

  /** {@inheritDoc} */
  @Override
  public List<ProlificAuthor> getProlificAuthors(int minPublications) throws DAOException {
    try {
      Aggregation aggregation =
          Aggregation.newAggregation(
              // 1. Explode the publications array to analyze each entry individually
              Aggregation.unwind("publications_summary"),

              // 2. Group by Author (ID + Name) and Year to calculate annual productivity
              Aggregation.group("_id", "name", "publications_summary.year")
                  .count()
                  .as("paper_created"),

              // 3. Filter results to keep only year/author pairs meeting the threshold
              Aggregation.match(Criteria.where("paper_created").gt(minPublications)),

              // 4. Re-group to eliminate duplicate author records (if they were prolific in
              // multiple years)
              Aggregation.group("_id._id").first("_id.name").as("author_name"),

              // 5. Project the final fields to match the ProlificAuthor DTO
              Aggregation.project("author_name").and("_id").as("author_id"));

      return mongoTemplate
          .aggregate(aggregation, "authors", ProlificAuthor.class)
          .getMappedResults();

    } catch (Exception e) {
      throw new DAOException("Error executing prolific authors aggregation", e);
    }
  }
}
