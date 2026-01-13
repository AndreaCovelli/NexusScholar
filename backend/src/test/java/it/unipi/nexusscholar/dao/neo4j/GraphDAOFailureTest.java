package it.unipi.nexusscholar.dao.neo4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.utils.BetweennessEntry;
import it.unipi.nexusscholar.utils.LeidenCommunity;
import it.unipi.nexusscholar.utils.PageRankEntry;
import it.unipi.nexusscholar.utils.ShortestPathAuthors;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.ClientException;

@ExtendWith(MockitoExtension.class)
class GraphDAOFailureTest {

  @Mock private Driver driver;
  @Mock private Session session;

  private GraphDAO graphDAO;

  @BeforeEach
  void setUp() {
    graphDAO = new GraphDAO(driver);
  }

  @Test
  void savePaperNode_HandlesException() {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setId("p1");

    when(driver.session()).thenReturn(session);
    doThrow(new ClientException("Neo4j down")).when(session).executeWriteWithoutResult(any());

    boolean result = graphDAO.savePaperNode(paperDTO);
    assertFalse(result, "Should return false when DB throws exception");
  }

  @Test
  void savePaperNode_NullInput() {
    assertFalse(graphDAO.savePaperNode(null));
    assertFalse(graphDAO.savePaperNode(new PaperDTO())); // null ID
  }

  @Test
  void pageRankCount_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Connection error"));
    int count = graphDAO.pageRankCount();
    assertEquals(-1, count);
  }

  @Test
  void pageRankAlg_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Error"));
    List<PageRankEntry> res = graphDAO.pageRankAlg(0, 10);
    assertTrue(res.isEmpty());
  }

  @Test
  void leidenCount_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Connection error"));
    int count = graphDAO.leidenCount();
    assertEquals(-1, count);
  }

  @Test
  void leidenCommunityAlg_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Error"));
    List<LeidenCommunity> res = graphDAO.leidenCommunityAlg(0, 10);
    assertTrue(res.isEmpty());
  }

  @Test
  void betweennessCount_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Connection error"));
    int count = graphDAO.betweennessCount();
    assertEquals(-1, count);
  }

  @Test
  void betweennessAlg_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Error"));
    List<BetweennessEntry> res = graphDAO.betweennessAlg(0, 10);
    assertTrue(res.isEmpty());
  }

  @Test
  void shortestPathAlg_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Error"));
    ShortestPathAuthors res = graphDAO.shortestPathAlg("A", "B");
    assertNull(res);
  }

  @Test
  void connect_Failure() {
    doThrow(new RuntimeException("Fail")).when(driver).verifyConnectivity();
    assertFalse(graphDAO.connect());
  }
}
