package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.model.neo4j.AuthorNode;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

  private final Driver driver;

  @Autowired
  public GraphService(Driver driver) {
    this.driver = driver;
  }

  public boolean connect() {
    try {
      driver.verifyConnectivity();
      return true;
    } catch (Exception e) {
      System.err.println("Connection failed: " + e.getMessage());
      return false;
    }
  }

  /**
   * Overloaded connect method for backward compatibility. In the Spring Boot/Testcontainers
   * environment, credentials are handled by the injected Driver, so we delegate to the no-arg
   * connect().
   */
  public boolean connect(String user, String pass) {
    return connect();
  }

  public List<PageRankEntry> pagerank() {
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

      List<Record> lr =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run(
                        """
                    CALL gds.pageRank.stream('paperCitations')
                    YIELD nodeId,score
                    RETURN gds.util.asNode(nodeId).title as title, round(score,4) as rank
                    ORDER BY score DESC;""");
                return res.list();
              });

      List<PageRankEntry> resultPageRank = new ArrayList<>();
      for (Record r : lr) {
        resultPageRank.add(new PageRankEntry(r));
      }

      return resultPageRank;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  public ShortestPathAuthors collabPath(String author1, String author2) {
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
      System.err.println(e.getMessage());
      return null;
    }
  }

  public List<LeidenCommunity> hiddenCommunities() {
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
      System.err.println(e.getMessage());
      return null;
    }
  }

}
