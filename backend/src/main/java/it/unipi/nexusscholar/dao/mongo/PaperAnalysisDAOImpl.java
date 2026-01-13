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
 * Implementation of {@link PaperAnalysisDAO} using MongoDB Aggregation Framework.
 *
 * <p>This class uses {@link MongoTemplate} to construct and execute multi-stage aggregation
 * pipelines.
 */
@Repository
@RequiredArgsConstructor
public class PaperAnalysisDAOImpl implements PaperAnalysisDAO {

  private final MongoTemplate mongoTemplate;

  /**
   * {@inheritDoc}
   *
   * <p><b>Pipeline Logic:</b>
   *
   * <ol>
   *   <li><b>Unwind:</b> Deconstructs the 'fields_of_study' array so each topic becomes a separate
   *       document.
   *   <li><b>Group:</b> Groups by 'fields_of_study' and 'year', counting the occurrences.
   *   <li><b>Project:</b> Formats the output to match the {@link TrendAnalysis} structure.
   *   <li><b>Sort:</b> Sorts primarily by year (ASC) and secondarily by count (DESC).
   * </ol>
   */
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

              // 4. Sort results
              Aggregation.sort(
                  Sort.by(Sort.Direction.ASC, "year")
                      .and(Sort.by(Sort.Direction.DESC, "paper_created"))));

      return mongoTemplate.aggregate(aggregation, "papers", TrendAnalysis.class).getMappedResults();

    } catch (Exception e) {
      throw new DAOException("Error executing trend analysis aggregation", e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Pipeline Logic:</b>
   *
   * <ol>
   *   <li><b>Unwind:</b> Deconstructs the 'venue' array.
   *   <li><b>Group:</b> Groups by 'venue' and 'year', counting the papers.
   *   <li><b>Project:</b> Formats the output to match the {@link VenueAnalysis} structure.
   *   <li><b>Sort:</b> Sorts primarily by year (ASC) and secondarily by volume (DESC).
   * </ol>
   */
  @Override
  public List<VenueAnalysis> getVenueAnalysis() throws DAOException {
    try {
      Aggregation aggregation =
          Aggregation.newAggregation(
              // 1. Explode venue array
              Aggregation.unwind("venue"),

              // 2. Group by venue and year
              Aggregation.group("venue", "year").count().as("paper_created"),

              // 3. Project to flatten
              Aggregation.project("paper_created")
                  .and("_id.venue")
                  .as("venue")
                  .and("_id.year")
                  .as("year")
                  .andExclude("_id"),

              // 4. Sort
              Aggregation.sort(
                  Sort.by(Sort.Direction.ASC, "year")
                      .and(Sort.by(Sort.Direction.DESC, "paper_created"))));

      return mongoTemplate.aggregate(aggregation, "papers", VenueAnalysis.class).getMappedResults();

    } catch (Exception e) {
      throw new DAOException("Error executing venue analysis aggregation", e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Pipeline Logic:</b>
   *
   * <ol>
   *   <li><b>Project:</b> Calculates the size of the 'authors' array for each paper.
   *   <li><b>Group:</b> Groups by 'year' and calculates the average of the author counts.
   *   <li><b>Project:</b> Formats the output to match {@link CollaborationEvolution}.
   *   <li><b>Sort:</b> Sorts by year (ASC).
   * </ol>
   */
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
