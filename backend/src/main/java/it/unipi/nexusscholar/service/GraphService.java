package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.PathNodeDTO;
import it.unipi.nexusscholar.dto.neo4j.PathRelationshipDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.utils.BetweennessEntry;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service Layer for business logic related to graph analysis.
 *
 * <p>This class is responsible for invoking {@link GraphDAO} methods, handling high-level
 * exceptions, and transforming raw results into paginated DTOs (Data Transfer Objects) ready for
 * the controller.
 */
@Slf4j
@Service
public class GraphService {

  private final GraphDAO graphDAO;

  public GraphService(GraphDAO graphDAO) {
    this.graphDAO = graphDAO;
  }

  /**
   * Attempts to establish a connection to the graph database.
   *
   * @return {@code true} if the connection succeeds, {@code false} otherwise.
   */
  public boolean connect() {
    try {
      return graphDAO.connect();
    } catch (Exception e) {
      log.error("Failed to connect to GraphDAO", e);
      return false;
    }
  }

  /**
   * Retrieves PageRank algorithm results in a paginated format.
   *
   * @param pageable Pagination information (offset and page size).
   * @return A {@link Page} of {@link PageRankDTO}. Returns an empty page in case of error.
   */
  public Page<PageRankDTO> pagerank(Pageable pageable) {
    try {
      List<PageRankDTO> cont =
          graphDAO.pageRankAlg((int) pageable.getOffset(), pageable.getPageSize()).stream()
              .map(this::toPageRankDTO)
              .toList();
      return new PageImpl<>(cont, pageable, graphDAO.pageRankCount());
    } catch (Exception e) {
      log.error("Error executing PageRank algorithm", e);
      return Page.empty(pageable);
    }
  }

  /**
   * Finds the shortest collaboration path between two specific authors.
   *
   * @param author1 Name of the first author.
   * @param author2 Name of the second author.
   * @return {@link ShortestPathDTO} containing nodes and relationships of the path, or {@code null}
   *     if none exists.
   */
  public ShortestPathDTO collabPath(String author1, String author2) {
    try {
      ShortestPathAuthors spa = graphDAO.shortestPathAlg(author1, author2);
      if (spa == null) {
        return null;
      }
      return toShortestPathDTO(spa);
    } catch (Exception e) {
      log.error("Error finding shortest path between {} and {}", author1, author2, e);
      return null;
    }
  }

  /**
   * Retrieves communities detected via the Leiden algorithm (Hidden Communities).
   *
   * @param pageable Pagination information.
   * @return A {@link Page} of {@link LeidenDTO}.
   */
  public Page<LeidenDTO> hiddenCommunities(Pageable pageable) {
    try {
      // 1. Calculate pagination parameters
      int skip = (int) pageable.getOffset();
      int limit = pageable.getPageSize();

      // 2. Fetch only the requested slice from the database
      // Note: This requires the DAO update shown in Step 1
      List<LeidenDTO> pagedContent =
          graphDAO.leidenCommunityAlg(skip, limit).stream().map(this::toLeidenDTO).toList();

      // 3. Fetch total count for pagination metadata
      // Note: 'leidenCount()' calculates total unique communities
      int totalElements = graphDAO.leidenCount();

      // 4. Return the Page object
      return new PageImpl<>(pagedContent, pageable, totalElements);

    } catch (Exception e) {
      log.error("Error executing hidden communities algorithm (Leiden)", e);
      return Page.empty(pageable);
    }
  }

  /**
   * Retrieves Betweenness Centrality analysis results in a paginated format.
   *
   * @param pageable Pagination information.
   * @return A {@link Page} of {@link BetweennessDTO}.
   */
  public Page<BetweennessDTO> betweenness(Pageable pageable) {
    try {
      // 1. Pass pagination params to DAO (Database-level slicing)
      int skip = (int) pageable.getOffset();
      int limit = pageable.getPageSize();

      List<BetweennessDTO> res =
          graphDAO.betweennessAlg(skip, limit).stream().map(this::toBetweennessDTO).toList();

      // 2. Fetch total count for correct Page metadata
      int totalCount = graphDAO.betweennessCount();

      return new PageImpl<>(res, pageable, totalCount);
    } catch (Exception e) {
      log.error("Betweenness calculation failed", e);
      return Page.empty(pageable);
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

    Path path = spa.getShortestPath();

    List<PathNodeDTO> nodeDTOs = new ArrayList<>();
    for (Node node : path.nodes()) {
      PathNodeDTO nodeDTO = new PathNodeDTO();
      nodeDTO.setElementId(node.elementId());

      if (node.hasLabel("Author")) {
        nodeDTO.setType("Author");
        nodeDTO.setDisplayName(node.get("name").asString());
      } else if (node.hasLabel("Paper")) {
        nodeDTO.setType("Paper");
        nodeDTO.setDisplayName(node.get("title").asString());
      }
      nodeDTOs.add(nodeDTO);
    }
    dto.setNodes(nodeDTOs);

    List<PathRelationshipDTO> relDTOs = new ArrayList<>();
    for (Relationship rel : path.relationships()) {
      PathRelationshipDTO relDTO = new PathRelationshipDTO();
      relDTO.setElementId(rel.elementId());
      relDTO.setType(rel.type());
      relDTO.setStartNodeId(rel.startNodeElementId());
      relDTO.setEndNodeId(rel.endNodeElementId());
      relDTOs.add(relDTO);
    }
    dto.setRelationships(relDTOs);

    return dto;
  }

  private BetweennessDTO toBetweennessDTO(BetweennessEntry entry) {
    BetweennessDTO dto = new BetweennessDTO();
    dto.setPaperTitle(entry.getTitle());
    dto.setBetweenness(entry.getBetweenness());
    return dto;
  }
}
