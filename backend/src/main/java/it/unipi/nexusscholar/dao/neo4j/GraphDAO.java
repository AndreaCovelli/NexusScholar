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
import org.springframework.stereotype.Repository;

@Slf4j // Add Lombok annotation
@Repository
public class GraphDAO {

  private final Driver driver;

  public GraphDAO(Driver driver) {
    this.driver = driver;
  }

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
                Result res =
                    tx.run(
                        """
                                                CALL gds.pageRank.stream('paperCitations')
                                                YIELD nodeId,score
                                                RETURN gds.util.asNode(nodeId).title as title, round(score,4) as rank
                                                ORDER BY score DESC
                                                SKIP $s
                                                LIMIT $l;""",
                        Map.of("s", skip, "l", limit));
                return res.list();
              });

      return lr.stream().map(PageRankEntry::new).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("PageRank calculation failed", e);
      return Collections.emptyList();
    }
  }

  public int pageRankCount() {
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

      return session.executeRead(
          tx -> {
            // FIX: Use gds.graph.list to get metadata count instead of invalid stream yield
            Result res =
                tx.run(
                    """
                                        CALL gds.graph.list('paperCitations')
                                        YIELD nodeCount
                                        RETURN nodeCount;
                                        """);
            return res.hasNext() ? res.single().get("nodeCount").asInt() : 0;
          });

    } catch (Exception e) {
      log.error("PageRank calculation failed", e);
      return -1;
    }
  }

  public ShortestPathAuthors shortestPathAlg(String author1, String author2) {
    if (author1 == null || author1.isEmpty() || author2 == null || author2.isEmpty()) return null;

    try (Session session = driver.session()) {
      Record r =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run(
                        """
                                                MATCH (a1:Author{name:$a1Name}),
                                                      (a2:Author{name:$a2Name}),
                                                      path=shortestPath(
                                                        (a1) - [:AUTHORED*] - (a2)
                                                      )
                                                RETURN path,
                                                       length(path)/2 as DegreeSeparation
                                                ORDER BY DegreeSeparation ASC
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

  public List<LeidenCommunity> leidenCommunityAlg() {
    try (Session session = driver.session()) {
      // 1. Project the graph using Native Projection with UNDIRECTED orientation.
      // We drop the graph first to ensure we don't use an existing directed version from previous
      // failed runs.
      session.executeWriteWithoutResult(
          tx -> {
            tx.run("CALL gds.graph.drop('coAuthors', false)");
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

      // 2. Stream Leiden results
      // Since the projection is bipartite (Author-Paper), we filter WHERE node:Author
      List<Record> lr =
          session.executeRead(
              tx -> {
                Result r =
                    tx.run(
                        """
                                                 CALL gds.leiden.stream(
                                                     'coAuthors',
                                                     {
                                                         randomSeed: 42
                                                     }
                                                 )
                                                 YIELD nodeId, communityId
                                                 WITH communityId, gds.util.asNode(nodeId) as node
                                                 WHERE node:Author
                                                 RETURN communityId,
                                                        node.name as name
                                                 """);
                return r.list();
              });

      // 3. Aggregate results into communities
      Map<Integer, LeidenCommunity> communities = new HashMap<>();
      for (Record r : lr) {
        int communityId = r.get("communityId").asInt();
        String name = r.get("name").asString();

        communities.computeIfAbsent(communityId, LeidenCommunity::new).addAuthor(name);
      }

      return new ArrayList<>(communities.values());
    } catch (Exception e) {
      log.error("Leiden Community detection failed", e);
      return Collections.emptyList();
    }
  }

  public int leidenCount() {
    try (Session session = driver.session()) {
      return session.executeRead(
          tx -> {
            // FIX: Use gds.graph.list to get metadata count
            Result r =
                tx.run(
                    """
                                        CALL gds.graph.list('coAuthors')
                                        YIELD nodeCount
                                        RETURN nodeCount;
                                        """);
            return r.hasNext() ? r.single().get("nodeCount").asInt() : 0;
          });
    } catch (Exception e) {
      log.error("Count for leiden not achievable");
      return -1;
    }
  }

  public List<BetweennessEntry> betweennessAlg() {
    try (Session session = driver.session()) {
      // Step 1: Check if projection exists
      boolean exists =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run("CALL gds.graph.exists('paperCitations') YIELD exists RETURN exists");
                return res.single().get("exists").asBoolean();
              });

      // Step 2: Create projection if missing
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

      // Step 3: Validate projection has data (CRITICAL DIAGNOSTIC)
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

      // Step 4: Run betweenness with sampling for performance
      List<Record> records =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run(
                        """
                                                CALL gds.betweenness.stream('paperCitations', {
                                                    samplingSize: 1000,
                                                    samplingSeed: 42
                                                })
                                                YIELD nodeId, score
                                                RETURN gds.util.asNode(nodeId).title AS title,
                                                       round(score, 4) AS betweenness
                                                ORDER BY score DESC
                                                LIMIT 100
                                                """);
                return res.list();
              });

      return records.stream().map(BetweennessEntry::new).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Betweenness calculation failed", e);
      return Collections.emptyList();
    }
  }

  public int betweennessCount() {
    try (Session session = driver.session()) {
      return session.executeRead(
          tx -> {
            // FIX: Use gds.graph.list to get metadata count.
            // Also fixed method name typo (betwenness -> betweenness)
            Result res =
                tx.run(
                    """
                                        CALL gds.graph.list('paperCitations')
                                        YIELD nodeCount
                                        RETURN nodeCount;
                                        """);
            return res.hasNext() ? res.single().get("nodeCount").asInt() : 0;
          });
    } catch (Exception e) {
      log.error("Count for betweenness not achievable", e);
      return -1;
    }
  }

  public boolean savePaperNode(PaperDTO p) {
    if (p == null || p.getId() == null) return false;

    try (Session session = driver.session()) {
      session.executeWriteWithoutResult(
          tx -> {
            tx.run(
                """
                                MERGE (p:Paper{paperID:$paperID})
                                SET p.title = $title
                                WITH p
                                OPTIONAL MATCH p <- [r:AUTHORED] - (:Author)
                                DELETE r
                                WITH p
                                UNWIND $authors as author
                                MERGE (a:Author{id:author.id}
                                ON CREATE SET a.name = author.name
                                MERGE (p:Paper <- [:AUTHORED] - author:Author)
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
    } catch (Exception e) {
      log.error("Save paper node failed", e);
      return false;
    }
  }
}
