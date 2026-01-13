package it.unipi.nexusscholar.dao;

import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import java.util.List;

/**
 * Data access interface for performing complex analytical queries on Author data.
 *
 * <p>Unlike the standard {@code AuthorDAO}, this interface handles operations that require MongoDB
 * Aggregation Pipelines (grouping, unwinding, etc.).
 */
public interface AuthorAnalysisDAO {

  /**
   * Identifies "Prolific Authors" based on their publication output within a single year.
   *
   * <p>This method executes an aggregation pipeline that:
   *
   * <ol>
   *   <li>Unwinds the publication history.
   *   <li>Groups by Author and Year to count annual output.
   *   <li>Filters for years where output exceeded the {@code minPublications} threshold.
   *   <li>Groups back by Author to return unique individuals.
   * </ol>
   *
   * @param minPublications The minimum number of publications in a given context (e.g., per year)
   *     required to be included.
   * @return A list of {@link ProlificAuthor} objects representing the identified researchers.
   * @throws DAOException If the database aggregation fails.
   */
  List<ProlificAuthor> getProlificAuthors(int minPublications) throws DAOException;
}
