package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphServiceExceptionTest {

  @Mock private GraphDAO mockGraphDAO;

  private GraphService graphService;

  @BeforeEach
  void setUp() {
    graphService = new GraphService(mockGraphDAO);
  }

  @Test
  void testConnectReturnsFalseOnException() {
    doThrow(new RuntimeException("Connection failed")).when(mockGraphDAO).connect();

    boolean result = graphService.connect();

    assertFalse(result, "connect() should return false when DAO throws exception");
  }

  @Test
  void testPageRankReturnsEmptyListOnException() {
    when(mockGraphDAO.pageRankAlg()).thenThrow(new RuntimeException("PageRank failed"));

    List<PageRankDTO> results = graphService.pagerank();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testCollabPathReturnsNullOnException() {
    when(mockGraphDAO.shortestPathAlg(anyString(), anyString()))
        .thenThrow(new RuntimeException("Path calculation failed"));

    ShortestPathDTO result = graphService.collabPath("Author1", "Author2");

    assertNull(result);
  }

  @Test
  void testHiddenCommunitiesReturnsEmptyListOnException() {
    when(mockGraphDAO.leidenCommunityAlg()).thenThrow(new RuntimeException("Leiden failed"));

    List<LeidenDTO> results = graphService.hiddenCommunities();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testBetweennessReturnsEmptyListOnException() {
    when(mockGraphDAO.betweennessAlg()).thenThrow(new RuntimeException("Betweenness failed"));

    List<BetweennessDTO> results = graphService.betweenness();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testPageRankWithEmptyResults() {
    when(mockGraphDAO.pageRankAlg()).thenReturn(Collections.emptyList());

    List<PageRankDTO> results = graphService.pagerank();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testCollabPathReturnsNullWhenDAOReturnsNull() {
    when(mockGraphDAO.shortestPathAlg("Author1", "Author2")).thenReturn(null);

    ShortestPathDTO result = graphService.collabPath("Author1", "Author2");

    assertNull(result);
  }
}
