package it.unipi.nexusscholar.dao.neo4j;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.utils.BetweennessEntry;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object (DAO) for interacting with the Neo4j database.
 *
 * <p>This class is responsible for managing Graph Projections and executing Neo4j Graph Data
 * Science (GDS) algorithms, including PageRank, Shortest Path, Leiden Community Detection, and
 * Betweenness Centrality.
 */
@Slf4j
@Repository
public class GraphDAO {

  private final Driver driver;

  /**
   * Constructor for injecting the Neo4j driver.
   *
   * @param driver The configured Neo4j driver.
   */
  public GraphDAO(Driver driver) {
    this.driver = driver;
  }

  /**
   * Verifies connectivity to the Neo4j database.
   *
   * @return {@code true} if the connection is established successfully, {@code false} otherwise.
   */
  public boolean connect() {
    try {
      driver.verifyConnectivity();
      return true;
    } catch (Exception e) {
      // Best Practice: Log the message AND the exception to capture the stack trace
      // SLF4J handles the formatting; do not concatenate strings manually.
      log.error("Neo4j connection failed: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * Executes the PageRank algorithm on the citation graph projection. Creates the 'paperCitations'
   * projection if it does not exist.
   *
   * @param skip Number of results to skip (for pagination).
   * @param limit Maximum number of results to return.
   * @return A list of {@link PageRankEntry} containing papers and their scores, ordered by rank
   *     descending.
   */
  public List<PageRankEntry> pageRankAlg(int skip, int limit) {
    try (Session session = driver.session()) {
      boolean ex =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run("CALL gds.graph.exists('paperCitations') YIELD exists RETURN exists");
                return res.single().get("exists").asBoolean();
              });

      if (!ex) {
        session.executeWriteWithoutResult(
            tx -> {
              tx.run(
                  """
                                    CALL gds.graph.project(
                                      'paperCitations',
                                      'Paper',
                                      'CITES'
                                    );
                                    """);
            });
      }

      List<org.neo4j.driver.Record> lr =
          session.executeRead(
              tx -> {
                // OPTIMIZATION: Defer property lookup. Sort using lightweight ID/Score first.
                Result res =
                    tx.run(
                        """
                                                CALL gds.pageRank.stream('paperCitations')
                                                YIELD nodeId, score
                                                WITH nodeId, score
                                                ORDER BY score DESC
                                                SKIP $s
                                                LIMIT $l
                                                // Fetch properties only for the paginated results (Minimizes Disk I/O)
                                                RETURN gds.util.asNode(nodeId).title as title, round(score, 4) as rank
                                                """,
                        Map.of("s", skip, "l", limit));
                return res.list();
              });

      return lr.stream().map(PageRankEntry::new).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("PageRank calculation failed", e);
      return Collections.emptyList();
    }
  }

  /**
   * Counts the total number of nodes in the projection used for PageRank. Useful for calculating
   * pagination metadata.
   *
   * @return The total number of nodes, or -1 in case of error.
   */
  public int pageRankCount() {
    try (Session session = driver.session()) {
      // OPTIMIZATION: Avoid graph projection check for count.
      // Since the graph projection contains all Papers, counting nodes in the store is O(1) and
      // correct.
      return session.executeRead(
          tx -> tx.run("MATCH (:Paper) RETURN count(*) AS c").single().get("c").asInt());
    } catch (Exception e) {
      log.error("PageRank count failed", e);
      return -1;
    }
  }

  /**
   * Calculates the shortest path between two authors. Uses the [:AUTHORED] relationship in an
   * undirected or bidirectional manner through papers.
   *
   * @param author1 Name of the first author.
   * @param author2 Name of the second author.
   * @return A {@link ShortestPathAuthors} object representing the path, or {@code null} if not
   *     found.
   */
  public ShortestPathAuthors shortestPathAlg(String author1, String author2) {
    if (author1 == null || author1.isEmpty() || author2 == null || author2.isEmpty()) return null;

    try (Session session = driver.session()) {
      Record r =
          session.executeRead(
              tx -> {
                // OPTIMIZATION: Limit traversal depth to 10 to prevent "Small World"
                // explosion/timeouts.
                Result res =
                    tx.run(
                        """
                                                MATCH (a1:Author {name: $a1Name})
                                                MATCH (a2:Author {name: $a2Name})
                                                MATCH path = shortestPath((a1)-[:AUTHORED*..10]-(a2))
                                                RETURN path, length(path)/2 as DegreeSeparation
                                                LIMIT 1
                                                """,
                        Map.of("a1Name", author1, "a2Name", author2));
                return res.hasNext() ? res.single() : null;
              });

      if (r != null) return new ShortestPathAuthors(r);

      return null;
    } catch (Exception e) {
      log.error("Shortest path calculation failed", e);
      return null;
    }
  }

  /**
   * Executes the Leiden algorithm for author community detection. Relies on a 'coAuthors'
   * projection.
   *
   * @param skip Number of communities to skip.
   * @param limit Maximum number of communities to return.
   * @return A list of {@link LeidenCommunity} containing the community ID and member authors.
   */
  public List<LeidenCommunity> leidenCommunityAlg(int skip, int limit) {
    try (Session session = driver.session()) {
      ensureCoAuthorsGraph(session);

      return session.executeRead(
          tx -> {
            Result r =
                tx.run(
                    """
                                        CALL gds.leiden.stream('coAuthors', { randomSeed: 42 })
                                        YIELD nodeId, communityId
                                        WITH communityId, gds.util.asNode(nodeId) as node
                                        WHERE node:Author
                                        // Aggregate authors per community HERE
                                        WITH communityId, collect(node.name) as authors
                                        RETURN communityId, authors
                                        ORDER BY communityId ASC
                                        SKIP $skip
                                        LIMIT $limit
                                        """,
                    Map.of("skip", skip, "limit", limit));

            return r.list().stream()
                .map(
                    record ->
                        new LeidenCommunity(
                            record.get("communityId").asInt(),
                            record.get("authors").asList(Value::asString)))
                .collect(Collectors.toList());
          });
    } catch (Exception e) {
      log.error("Leiden Community detection failed", e);
      return Collections.emptyList();
    }
  }

  /**
   * Counts the total number of unique communities identified by the Leiden algorithm.
   *
   * @return The count of communities, or -1 in case of error.
   */
  public int leidenCount() {
    try (Session session = driver.session()) {
      ensureCoAuthorsGraph(session);

      return session.executeRead(
          tx -> {
            Result r =
                tx.run(
                    """
                                        CALL gds.leiden.stream('coAuthors', { randomSeed: 42 })
                                        YIELD communityId
                                        RETURN count(DISTINCT communityId) as communityCount;
                                        """);
            return r.hasNext() ? r.single().get("communityCount").asInt() : 0;
          });
    } catch (Exception e) {
      log.error("Count for leiden not achievable");
      return -1;
    }
  }

  /**
   * Ensures that the 'coAuthors' graph projection exists in GDS memory. Projects Author and Paper
   * nodes with undirected AUTHORED relationships.
   *
   * @param session The current Neo4j session.
   */
  private void ensureCoAuthorsGraph(Session session) {
    boolean exists =
        session.executeRead(
            tx -> {
              Result res = tx.run("CALL gds.graph.exists('coAuthors') YIELD exists RETURN exists");
              return res.single().get("exists").asBoolean();
            });

    if (!exists) {
      session.executeWriteWithoutResult(
          tx -> {
            tx.run(
                """
                                CALL gds.graph.project(
                                    'coAuthors',
                                    ['Author', 'Paper'],
                                    {
                                        AUTHORED: {
                                            orientation: 'UNDIRECTED'
                                        }
                                    }
                                )
                                """);
          });
    }
  }

  /**
   * Executes the Betweenness Centrality algorithm on the citation projection. Uses sampling for
   * efficiency on large graphs.
   *
   * @param skip Number of results to skip.
   * @param limit Maximum number of results to return.
   * @return A list of {@link BetweennessEntry} with papers and their centrality scores.
   */
  public List<BetweennessEntry> betweennessAlg(int skip, int limit) {
    try (Session session = driver.session()) {

      // 1. Ensure the Graph Projection exists
      boolean exists =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run("CALL gds.graph.exists('paperCitations') YIELD exists RETURN exists");
                return res.single().get("exists").asBoolean();
              });

      if (!exists) {
        session.executeWriteWithoutResult(
            tx -> {
              tx.run(
                  """
                                    CALL gds.graph.project(
                                        'paperCitations',
                                        'Paper',
                                        'CITES'
                                    )
                                    """);
            });
      }

      // 2. Check for empty graph
      Record stats =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run(
                        """
                                                CALL gds.graph.list('paperCitations')
                                                YIELD nodeCount, relationshipCount
                                                RETURN nodeCount, relationshipCount
                                                """);
                return res.hasNext() ? res.single() : null;
              });

      if (stats == null || stats.get("relationshipCount").asLong() == 0) {
        log.warn("Graph projection has 0 relationships. Betweenness cannot be calculated.");
        return Collections.emptyList();
      }

      // 3. Execute Algorithm with Pagination
      List<Record> records =
          session.executeRead(
              tx -> {
                // OPTIMIZATION: Defer property lookup.
                Result res =
                    tx.run(
                        """
                                                CALL gds.betweenness.stream('paperCitations', {
                                                    samplingSize: 1000,
                                                    samplingSeed: 42
                                                })
                                                YIELD nodeId, score
                                                WITH nodeId, score
                                                ORDER BY score DESC
                                                SKIP $skip
                                                LIMIT $limit
                                                RETURN gds.util.asNode(nodeId).title AS title,
                                                       round(score, 4) AS betweenness
                                                """,
                        Map.of("skip", skip, "limit", limit));
                return res.list();
              });

      return records.stream().map(BetweennessEntry::new).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Betweenness calculation failed", e);
      return Collections.emptyList();
    }
  }

  /**
   * Counts the total number of nodes considered for Betweenness Centrality.
   *
   * @return The node count, or -1 in case of error.
   */
  public int betweennessCount() {
    // OPTIMIZATION: Reuse O(1) store count.
    return pageRankCount();
  }

  /**
   * Saves or updates a Paper node and its relationships with Authors in the graph.
   *
   * @param p The Paper DTO containing data and authors.
   * @return {@code true} if the operation is successful, {@code false} otherwise.
   */
  public boolean savePaperNode(PaperDTO p) {
    if (p == null || p.getId() == null) return false;

    try (Session session = driver.session()) {
      session.executeWriteWithoutResult(
          tx -> {
            // OPTIMIZATION: Use "Delta" approach to reduce transaction log churn.
            // 1. MERGE Paper
            // 2. Delete ONLY obsolete relationships (authors NOT in the new list)
            // 3. MERGE new authors and relationships
            tx.run(
                """
                                MERGE (p:Paper {paperID: $paperID})
                                SET p.title = $title

                                WITH p
                                // 1. Delete relationships ONLY for authors NOT in the new list
                                OPTIONAL MATCH (p)<-[r:AUTHORED]-(oldA:Author)
                                WHERE NOT oldA.authorId IN [x IN $authors | x.id]
                                DELETE r

                                // 2. Merge (create if missing) incoming authors
                                WITH p
                                UNWIND $authors as authorData
                                MERGE (a:Author {authorId: authorData.id})
                                SET a.name = authorData.name

                                // 3. Create relationship only if missing
                                MERGE (p)<-[:AUTHORED]-(a)
                                """,
                Map.of(
                    "paperID",
                    p.getId(),
                    "title",
                    p.getTitle(),
                    "authors",
                    p.getAuthors().stream()
                        .map(a -> Map.of("id", a.getId(), "name", a.getName()))
                        .toList()));
          });

      return true;
    } catch (org.neo4j.driver.exceptions.Neo4jException e) {
      log.error("Save paper node failed", e);
      return false;
    }
  }
}
