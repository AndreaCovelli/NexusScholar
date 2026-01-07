package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.neo4j.GraphDAO;
import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    when(mockGraphDAO.pageRankAlg(anyInt(), anyInt()))
        .thenThrow(new RuntimeException("PageRank failed"));

    Page<PageRankDTO> results = graphService.pagerank(PageRequest.of(0, 10));

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

    Page<LeidenDTO> results = graphService.hiddenCommunities(PageRequest.of(0, 10));

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testBetweennessReturnsEmptyListOnException() {
    when(mockGraphDAO.betweennessAlg()).thenThrow(new RuntimeException("Betweenness failed"));

    Page<BetweennessDTO> results = graphService.betweenness(PageRequest.of(0, 10));

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testPageRankWithEmptyResults() {
    when(mockGraphDAO.pageRankAlg(anyInt(), anyInt())).thenReturn(Collections.emptyList());

    Page<PageRankDTO> results = graphService.pagerank(PageRequest.of(0, 10));

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
