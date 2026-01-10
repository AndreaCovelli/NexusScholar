package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.dao.RegisteredUserAnalysisDAO;
import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class RegisteredUserAnalysisDAOImpl implements RegisteredUserAnalysisDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<PaperLeaderboard> getMostBookmarkedPapers(int year, int month) {

        Aggregation aggregation = newAggregation(
                // 1. Project: Rename 'bookmarked_papers' array to singular 'bookmarked_paper'
                project().and("bookmarked_papers").as("bookmarked_paper").andExclude("_id"),

                // 2. Unwind: Deconstruct the array to process individual bookmarks
                unwind("bookmarked_paper"),

                // 3. Match: Filter papers saved in the specific Year and Month
                match(Criteria.expr(
                        BooleanOperators.And.and(
                                ComparisonOperators.valueOf(DateOperators.Year.yearOf("bookmarked_paper.saved_at")).equalToValue(year),
                                ComparisonOperators.valueOf(DateOperators.Month.monthOf("bookmarked_paper.saved_at")).equalToValue(month)
                        )
                )),

                // 4. Group: Count bookmarks per paper (grouping by ID and Title)
                group("bookmarked_paper.paper_id", "bookmarked_paper.title")
                        .count().as("bookmarked_received"),

                // 5. Project: Format the output to match PaperLeaderboard class structure
                project()
                        .and("_id.paper_id").as("paper_id")
                        .and("_id.title").as("title")
                        .and("bookmarked_received").as("bookmarked_received")
                        .andExclude("_id"),

                // 6. Sort: Order by most bookmarks (Descending)
                sort(Sort.Direction.DESC, "bookmarked_received"),

                // 7. Limit: Get top 5 results
                limit(5)
        );

        return mongoTemplate.aggregate(aggregation, "registeredUsers", PaperLeaderboard.class)
                .getMappedResults();
    }
}