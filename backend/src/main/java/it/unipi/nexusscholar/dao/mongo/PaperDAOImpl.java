package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.dao.PaperDAOCustom;
import java.util.List;

import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaperDAOImpl implements PaperDAOCustom {
  private final MongoTemplate mongoTemplate;


  @Override
  public List<TrendAnalysis> getTrendAnalysis() {
    Aggregation aggregation =
        Aggregation.newAggregation(
            // 1. Explode the array of fields of study to analyze each entru
            Aggregation.unwind("fields_of_study"),
            // 2. Group by Field of study and year
            Aggregation.group("fields_of_study", "year").count().as("paper_created"),
            // 3. Project to avoid the composite field _id
            Aggregation.project("paper_created")
                .and("_id.fields_of_study").as("field_of_study")
                .and("_id.year").as("year")
                .andExclude("_id"),
            // 4. Ordering descending by year
            Aggregation.sort(Sort.Direction.ASC, "year"));
      return mongoTemplate.aggregate(aggregation, "papers", TrendAnalysis.class).getMappedResults();
  }


  @Override
  public List<VenueAnalysis> getVenueAnalysis() {
    Aggregation aggregation =
        Aggregation.newAggregation(
            // 1. Explode the array of venues to analyze each entry
            Aggregation.unwind("venue"),
            // 2. Group by venue and year
            Aggregation.group("venue", "year").count().as("paper_created"),
            // 3. Project to avoid the composite field _id
            Aggregation.project("paper_created")
                .and("_id.venue").as("venue")
                .and("_id.year").as("year")
                .andExclude("_id"),
            // 4. Ordering ascending by year and descending by paper_created
            Aggregation.sort(
                Sort.by(Sort.Direction.ASC, "year")
                .and(Sort.by(Sort.Direction.DESC, "paper_created"))));
      return mongoTemplate.aggregate(aggregation, "papers", VenueAnalysis.class).getMappedResults();
  }

    @Override
    public List<CollaborationEvolution> getCollaborationEvolution() {
        Aggregation aggregation =
                Aggregation.newAggregation(
                        //1. Project each document with year and compute num_authors
                        Aggregation.project("year").and("authors").size().as("num_authors"),
                        //2. Group by year
                        Aggregation.group("year").avg("num_authors").as("avg_authors"),
                        // 3. Project to adjust the fields
                        Aggregation.project("avg_authors")
                                .and("_id").as("year")
                                .andExclude("_id"),
                        //4. Ordering ascending by year
                        Aggregation.sort(Sort.Direction.ASC, "year")
                );
        return mongoTemplate.aggregate(aggregation, "papers", CollaborationEvolution.class).getMappedResults();
    }

}
