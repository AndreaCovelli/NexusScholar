package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.neo4j.BetweennesDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.utils.BetweennessEntry;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.*;
import java.util.stream.Collectors;
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

  public List<PageRankDTO> pagerank() {
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

      List<PageRankDTO> resultPageRankDTO = new ArrayList<>();
      for (PageRankEntry e : resultPageRank) {
        resultPageRankDTO.add(toPageRankDTO(e));
      }

      return resultPageRankDTO;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  public ShortestPathDTO collabPath(String author1, String author2) {
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

      if (r != null){
          ShortestPathAuthors sss = new ShortestPathAuthors(r);
          return toShortestPathDTO(sss);
      }
      return null;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  public List<LeidenDTO> hiddenCommunities() {
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

      List<LeidenCommunity> ls = new ArrayList<>(communities.values());

      List<LeidenDTO> ldto = new ArrayList<>();
      for(LeidenCommunity lc : ls){
            ldto.add(toLeidenDTO(lc));
      }

      return ldto;

    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  /**
   * Calculates betweenness papers in the collaboration network. Betweenness centrality identifies
   * "gatekeeper" papers who sits on information flow by being on the shortest paths between the
   * highest number of citation pairs.
   *
   * @return List of top 100 papers ranked by betweenness centrality score, or an empty list on
   *     error
   */
  public List<BetweennesDTO> betweenness() {
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
        System.err.println("WARNING: Graph projection has 0 relationships");
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

      List<BetweennessEntry> lbe = records.stream().map(BetweennessEntry::new).collect(Collectors.toList());

      List<BetweennesDTO> lbto = new ArrayList<>();
      for (BetweennessEntry be : lbe) {
            lbto.add(toBetweennesDTO(be));
      }

      return lbto;

    } catch (Exception e) {
      System.err.println("Betweenness calculation failed: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  private PageRankDTO toPageRankDTO(PageRankEntry entry) {
      PageRankDTO dto = new PageRankDTO();
      dto.setRank(entry.getRank());
      dto.setPaperTitle(entry.getPaperTitle());
      return dto;
  }

  private LeidenDTO toLeidenDTO(LeidenCommunity community) {
      LeidenDTO dto = new LeidenDTO();
      dto.setCommunityId(community.getCommunityId());
      dto.setAuthors(community.getAuthors());
      return dto;
  }

  private ShortestPathDTO toShortestPathDTO(ShortestPathAuthors spa) {
      ShortestPathDTO dto = new ShortestPathDTO();
      dto.setDegreeSeparation(spa.getDegreeSeparation());
      dto.setEdges(spa.getShortestPath().relationships().toString());
      dto.setNodes(spa.getShortestPath().nodes().toString());
      return dto;
  }


  private BetweennesDTO toBetweennesDTO(BetweennessEntry entry) {
      BetweennesDTO dto = new BetweennesDTO();
      dto.setPaperTitle(entry.getTitle());
      dto.setBetweenness(entry.getBetweenness());
      return dto;
  }


}
