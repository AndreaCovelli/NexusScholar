package it.unipi.nexusscholar.dao.neo4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dto.mongo.PaperDTO;
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
  void pageRankCount_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Connection error"));
    int count = graphDAO.pageRankCount();
    assertEquals(-1, count);
  }

  @Test
  void leidenCount_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Connection error"));
    int count = graphDAO.leidenCount();
    assertEquals(-1, count);
  }

  @Test
  void betweennessCount_HandlesException() {
    when(driver.session()).thenThrow(new RuntimeException("Connection error"));
    int count = graphDAO.betweennessCount();
    assertEquals(-1, count);
  }
}
