package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.utils.BetweennessEntry;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

  private final GraphDAO graphDAO;

  public GraphService(GraphDAO graphDAO) {
    this.graphDAO = graphDAO;
  }

  public boolean connect() {
    try {
      graphDAO.connect();
      return true;
    } catch (Exception e) {
      System.err.println("GraphDAO not connected: " + e.getMessage());
      return false;
    }
  }

  public List<PageRankDTO> pagerank() {

    try {
      List<PageRankEntry> resultPageRank = graphDAO.pageRankAlg();

      if (resultPageRank.isEmpty()) return null;

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
    try {
      return toShortestPathDTO(graphDAO.shortestPathAlg(author1, author2));
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  public List<LeidenDTO> hiddenCommunities() {

    try {
      List<LeidenCommunity> ls = graphDAO.leidenCommunityAlg();

      if (ls.isEmpty()) return null;

      List<LeidenDTO> ldto = new ArrayList<>();
      for (LeidenCommunity lc : ls) {
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
  public List<BetweennessDTO> betweenness() {

    try {
      List<BetweennessEntry> lbe = graphDAO.betweennessAlg();

      if (lbe.isEmpty()) return null;

      List<BetweennessDTO> lbto = new ArrayList<>();
      for (BetweennessEntry be : lbe) {
        lbto.add(toBetweennessDTO(be));
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

  private BetweennessDTO toBetweennessDTO(BetweennessEntry entry) {
    BetweennessDTO dto = new BetweennessDTO();
    dto.setPaperTitle(entry.getTitle());
    dto.setBetweenness(entry.getBetweenness());
    return dto;
  }
}
