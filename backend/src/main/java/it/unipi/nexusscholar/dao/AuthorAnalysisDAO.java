package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import java.util.List;

/**
 * Data access interface for author-related analytical queries. These operations use MongoDB
 * aggregation pipelines.
 */
public interface AuthorAnalysisDAO {

  /**
   * Prolific Author Identification: Finds authors who have published more than the specified
   * minimum number of papers.
   *
   * <p>MongoDB Aggregation Pipeline:
   *
   * <pre>
   * db.authors.aggregate([
   *   { $unwind: "$publications_summary" },
   *   { $group: { _id: {_id: "$_id", name: "$name"}, paper_created: {$sum:1} } },
   *   { $match: { paper_created: {$gt: minPublications} } },
   *   { $group: { _id: "$_id._id", name: {$first: "$_id.name"} } }
   * ])
   * </pre>
   *
   * @param minPublications Minimum publication threshold
   * @return List of prolific authors exceeding the threshold
   * @throws DAOException if database operation fails
   */
  List<ProlificAuthor> getProlificAuthors(int minPublications) throws DAOException;
}
