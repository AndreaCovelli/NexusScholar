package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.dao.AuthorAnalysisDAO;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthorAnalysisDAOImpl implements AuthorAnalysisDAO {
  private final MongoTemplate mongoTemplate;

  @Override
  public List<ProlificAuthor> getProlificAuthors(int minPublications) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            // 1. Explode the array of publications to analyze each entry
            Aggregation.unwind("publications_summary"),
            // 2. Group by Author (ID + Name) and year e then count them
            Aggregation.group("_id", "name", "publications_summary.year")
                .count()
                .as("paper_created"),
            // 3. Filter by minimum paper_created
            Aggregation.match(Criteria.where("paper_created").gt(minPublications)),
            // 4. Group by to eliminate duplicate records
            Aggregation.group("_id._id").first("_id.name").as("author_name"),
            // 5. Project to correct assign name to the fields
            Aggregation.project("author_name").and("_id").as("author_id"));
    return mongoTemplate.aggregate(aggregation, "authors", ProlificAuthor.class).getMappedResults();
  }
}
