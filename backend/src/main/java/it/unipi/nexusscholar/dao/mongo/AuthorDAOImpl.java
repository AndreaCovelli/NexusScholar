package it.unipi.nexusscholar.dao.mongo;

import it.unipi.nexusscholar.dao.AuthorDAOCustom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthorDAOImpl implements AuthorDAOCustom {
  private final MongoTemplate mongoTemplate;

  /**
   * Implementation of: db.authors.aggregate([ { $unwind: "$publications_summary" }, { $group: {
   * _id: {_id: "$_id", name: "$name", year: "$year"}, paper_created: {$sum:1} } }, { $match: {
   * paper_created: {$gt: 5} } }, { $group: { _id: "$_id._id", name: {$first: "$_id.name"} } } ])
   */
  @Override
  public List<Document> getProlificAuthors(int minPublications) {
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
            // 4. Final group by to eliminate duplicate records
            Aggregation.group("_id._id").first("_id.name").as("name"));
    List<Document> authors =
        mongoTemplate.aggregate(aggregation, "authors", Document.class).getMappedResults();
    return authors;
  }


}
