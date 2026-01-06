package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.dao.PaperAnalysisDAO;
import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of paper analysis operations. Implements complex aggregation pipelines for
 * research analytics.
 */
@Repository
@RequiredArgsConstructor
public class PaperAnalysisDAOImpl implements PaperAnalysisDAO {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<TrendAnalysis> getTrendAnalysis() throws DAOException {
    try {
      Aggregation aggregation =
          Aggregation.newAggregation(
              // 1. Explode fields_of_study array
              Aggregation.unwind("fields_of_study"),

              // 2. Group by field of study and year
              Aggregation.group("fields_of_study", "year").count().as("paper_created"),

              // 3. Project to flatten the composite _id
              Aggregation.project("paper_created")
                  .and("_id.fields_of_study")
                  .as("field_of_study")
                  .and("_id.year")
                  .as("year")
                  .andExclude("_id"),

              // 4. Sort by year ascending for time-series analysis
              Aggregation.sort(Sort.Direction.ASC, "year"));

      return mongoTemplate.aggregate(aggregation, "papers", TrendAnalysis.class).getMappedResults();

    } catch (Exception e) {
      throw new DAOException("Error executing trend analysis aggregation", e);
    }
  }

  @Override
  public List<VenueAnalysis> getVenueAnalysis() throws DAOException {
    try {
      Aggregation aggregation =
          Aggregation.newAggregation(
              // 1. Explode venue array
              Aggregation.unwind("venue"),

              // 2. Group by venue and year
              Aggregation.group("venue", "year").count().as("paper_created"),

              // 3. Project to flatten the composite _id
              Aggregation.project("paper_created")
                  .and("_id.venue")
                  .as("venue")
                  .and("_id.year")
                  .as("year")
                  .andExclude("_id"),

              // 4. Sort by year ascending, then by paper count descending
              Aggregation.sort(
                  Sort.by(Sort.Direction.ASC, "year")
                      .and(Sort.by(Sort.Direction.DESC, "paper_created"))));

      return mongoTemplate.aggregate(aggregation, "papers", VenueAnalysis.class).getMappedResults();

    } catch (Exception e) {
      throw new DAOException("Error executing venue analysis aggregation", e);
    }
  }

  @Override
  public List<CollaborationEvolution> getCollaborationEvolution() throws DAOException {
    try {
      Aggregation aggregation =
          Aggregation.newAggregation(
              // 1. Project year and compute author count per paper
              Aggregation.project("year").and("authors").size().as("num_authors"),

              // 2. Group by year and compute average authors
              Aggregation.group("year").avg("num_authors").as("avg_authors"),

              // 3. Project to adjust field names
              Aggregation.project("avg_authors").and("_id").as("year").andExclude("_id"),

              // 4. Sort by year ascending
              Aggregation.sort(Sort.Direction.ASC, "year"));

      return mongoTemplate
          .aggregate(aggregation, "papers", CollaborationEvolution.class)
          .getMappedResults();

    } catch (Exception e) {
      throw new DAOException("Error executing collaboration evolution aggregation", e);
    }
  }
}
