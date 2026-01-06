package it.unipi.nexusscholar.dao.neo4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.utils.BetweennessEntry;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

@ExtendWith(MockitoExtension.class)
class DaoTest {

  @Mock private Driver mockDriver;
  @Mock private Session mockSession;
  @Mock private Result mockResult;
  @Mock private org.neo4j.driver.Record mockRecord;
  @Mock private Value mockValue;

  private GraphDAO graphDAO;

  @BeforeEach
  void setUp() {
    graphDAO = new GraphDAO(mockDriver);
  }

  @Test
  void testConnectSuccess() {
    doNothing().when(mockDriver).verifyConnectivity();

    boolean result = graphDAO.connect();

    assertTrue(result, "Connection should succeed when driver verifies connectivity");
    verify(mockDriver).verifyConnectivity();
  }

  @Test
  void testConnectFailure() {
    doThrow(new ServiceUnavailableException("Connection refused"))
        .when(mockDriver)
        .verifyConnectivity();

    boolean result = graphDAO.connect();

    assertFalse(result, "Connection should fail when driver throws exception");
  }

  @Test
  void testPageRankAlgReturnsEmptyListOnException() {
    when(mockDriver.session()).thenThrow(new RuntimeException("Database error"));

    List<PageRankEntry> results = graphDAO.pageRankAlg();

    assertNotNull(results);
    assertTrue(results.isEmpty(), "Should return empty list on exception");
  }

  @Test
  void testPageRankAlgWithExistingProjection() {
    when(mockDriver.session()).thenReturn(mockSession);

    when(mockSession.executeRead(any(TransactionCallback.class)))
        .thenReturn(true)
        .thenReturn(Collections.emptyList());

    List<PageRankEntry> results = graphDAO.pageRankAlg();

    assertNotNull(results);
  }

  @Test
  void testShortestPathAlgWithNullAuthor1() {
    ShortestPathAuthors result = graphDAO.shortestPathAlg(null, "Author2");

    assertNull(result, "Should return null when author1 is null");
  }

  @Test
  void testShortestPathAlgWithEmptyAuthor1() {
    ShortestPathAuthors result = graphDAO.shortestPathAlg("", "Author2");

    assertNull(result, "Should return null when author1 is empty");
  }

  @Test
  void testShortestPathAlgWithNullAuthor2() {
    ShortestPathAuthors result = graphDAO.shortestPathAlg("Author1", null);

    assertNull(result, "Should return null when author2 is null");
  }

  @Test
  void testShortestPathAlgWithEmptyAuthor2() {
    ShortestPathAuthors result = graphDAO.shortestPathAlg("Author1", "");

    assertNull(result, "Should return null when author2 is empty");
  }

  @Test
  void testShortestPathAlgReturnsNullOnException() {
    when(mockDriver.session()).thenThrow(new RuntimeException("Database error"));

    ShortestPathAuthors result = graphDAO.shortestPathAlg("Author1", "Author2");

    assertNull(result, "Should return null on exception");
  }

  @Test
  void testShortestPathAlgNoPathFound() {
    when(mockDriver.session()).thenReturn(mockSession);
    when(mockSession.executeRead(any(TransactionCallback.class))).thenReturn(null);

    ShortestPathAuthors result = graphDAO.shortestPathAlg("Author1", "Author2");

    assertNull(result, "Should return null when no path exists");
  }

  @Test
  void testLeidenCommunityAlgReturnsEmptyListOnException() {
    when(mockDriver.session()).thenThrow(new RuntimeException("Database error"));

    List<LeidenCommunity> results = graphDAO.leidenCommunityAlg();

    assertNotNull(results);
    assertTrue(results.isEmpty(), "Should return empty list on exception");
  }

  @Test
  void testBetweennessAlgReturnsEmptyListOnException() {
    when(mockDriver.session()).thenThrow(new RuntimeException("Database error"));

    List<BetweennessEntry> results = graphDAO.betweennessAlg();

    assertNotNull(results);
    assertTrue(results.isEmpty(), "Should return empty list on exception");
  }

  @Test
  void testGraphDAOInstantiation() {
    GraphDAO dao = new GraphDAO(mockDriver);

    assertNotNull(dao, "GraphDAO should be instantiated successfully");
  }
}
