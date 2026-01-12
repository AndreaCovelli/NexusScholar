package it.unipi.nexusscholar.dao.mongo;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import it.unipi.nexusscholar.dao.RegisteredUserAnalysisDAO;
import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link RegisteredUserAnalysisDAO} using MongoDB Aggregation Framework.
 * <p>
 * This class utilizes {@link MongoTemplate} to construct and execute complex aggregation pipelines,
 * allowing for efficient data processing directly on the database server.
 * </p>
 */
@Repository
public class RegisteredUserAnalysisDAOImpl implements RegisteredUserAnalysisDAO {

  @Autowired private MongoTemplate mongoTemplate;

  /**
   * {@inheritDoc}
   * <p>
   * <b>Aggregation Pipeline Logic:</b>
   * <ol>
   * <li><b>Project:</b> Renames the 'bookmarked_papers' array to a singular field for unwinding.</li>
   * <li><b>Unwind:</b> Deconstructs the array so each bookmark becomes a separate document.</li>
   * <li><b>Match:</b> Filters documents where the bookmark date matches the given year and month.</li>
   * <li><b>Group:</b> Groups by paper ID and title, counting the occurrences to calculate popularity.</li>
   * <li><b>Project:</b> Formats the result into the structure expected by {@link PaperLeaderboard}.</li>
   * <li><b>Sort:</b> Orders the results by the count of bookmarks in descending order.</li>
   * <li><b>Limit:</b> Restricts the result set to the top 5 papers.</li>
   * </ol>
   * </p>
   */
  @Override
  public List<PaperLeaderboard> getMostBookmarkedPapers(int year, int month) {

    Aggregation aggregation =
            newAggregation(
                    // 1. Project: Rename 'bookmarked_papers' array to singular 'bookmarked_paper'
                    project().and("bookmarked_papers").as("bookmarked_paper").andExclude("_id"),

                    // 2. Unwind: Deconstruct the array to process individual bookmarks
                    unwind("bookmarked_paper"),

                    // 3. Match: Filter papers saved in the specific Year and Month
                    match(
                            Criteria.expr(
                                    BooleanOperators.And.and(
                                            ComparisonOperators.valueOf(
                                                            DateOperators.Year.yearOf("bookmarked_paper.saved_at"))
                                                    .equalToValue(year),
                                            ComparisonOperators.valueOf(
                                                            DateOperators.Month.monthOf("bookmarked_paper.saved_at"))
                                                    .equalToValue(month)))),

                    // 4. Group: Count bookmarks per paper (grouping by ID and Title)
                    group("bookmarked_paper.paper_id", "bookmarked_paper.title")
                            .count()
                            .as("bookmarked_received"),

                    // 5. Project: Format the output to match PaperLeaderboard class structure
                    project()
                            .and("_id.paper_id")
                            .as("paper_id")
                            .and("_id.title")
                            .as("title")
                            .and("bookmarked_received")
                            .as("bookmarked_received")
                            .andExclude("_id"),

                    // 6. Sort: Order by most bookmarks (Descending)
                    sort(Sort.Direction.DESC, "bookmarked_received"),

                    // 7. Limit: Get top 5 results
                    limit(5));

    return mongoTemplate
            .aggregate(aggregation, "registeredUsers", PaperLeaderboard.class)
            .getMappedResults();
  }
}