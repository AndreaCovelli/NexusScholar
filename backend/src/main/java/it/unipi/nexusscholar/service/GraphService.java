package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.utils.NSConstants;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.AuthenticationException;

public class GraphService {

  private Driver driver;

  /** Constructor that instatiates driver istance */
  public GraphService() {
    driver = null;
  }

  /**
   * Method that connects to neo4j database using autenthication
   *
   * @param user : Username of user that want to connect
   * @param pass : Password of user that want to connect
   * @return true if succeded, false otherwise
   */
  public boolean connect(String user, String pass) {
    // credential not inserted correctly
    if (user == null || pass == null || user.isEmpty() || pass.isEmpty()) return false;

    driver = GraphDatabase.driver(NSConstants.NEO4J_URI, AuthTokens.basic(user, pass));

    // we need to test the driver to check if credentials are correct

    try (Session session = driver.session()) {
      session.run("RETURN 1").consume();
      return true;
    } catch (AuthenticationException e) {
      // once here we can say credentials are not correct
      System.err.println("Username or password is invalid");
      driver.close();
      driver = null;
      return false;
    }
  }

  public List<PageRankEntry> pagerank() {
    if (driver == null) return null;

    try (Session session = driver.session()) {
      // here it follows the query for the graph projection
      boolean ex =
          session.executeRead(
              tx -> {
                Result res =
                    tx.run("CALL gds.graph.exists('paperCitations') YIELD exists RETURN exists");
                return res.single().get("exists").asBoolean();
              });

      if (!ex) {
        session.executeWrite(
            tx -> {
              Result res =
                  tx.run(
                      """
                            CALL gds.graph.project(
                            'paperCitations',
                            'Paper',
                            'CITES'
                            );
                            """);
              return null;
            });
      }

      // then we can also compute with pagerank algorithm
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

      // convert record type infos
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
    // verify user input
    if (author1 == null || author1.isEmpty() || author2 == null || author2.isEmpty()) return null;

    // find the shortest path using cypher
    try (Session session = driver.session()) {
      Record r =
          session.executeRead(
              tx -> {
                Result res = tx.run(
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
                return res.single();
              });

      if(r!=null)
          return new ShortestPathAuthors(r);

      return null;

    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }
}
