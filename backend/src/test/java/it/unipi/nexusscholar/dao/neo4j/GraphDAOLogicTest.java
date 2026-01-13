package it.unipi.nexusscholar.dao.neo4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

@ExtendWith(MockitoExtension.class)
class GraphDAOLogicTest {

  @Mock private Driver driver;
  @Mock private Session session;
  @Mock private Result result;
  @Mock private Record record;
  @Mock private Value value;

  private GraphDAO graphDAO;

  @BeforeEach
  void setUp() {
    graphDAO = new GraphDAO(driver);
  }

  @Test
  void pageRankAlg_GraphNotExists_CreatesProjection() {
    when(driver.session()).thenReturn(session);

    // First read: check exists -> returns false
    when(session.executeRead(any()))
        .thenAnswer(
            invocation -> {
              TransactionCallback cb = invocation.getArgument(0);
              // We need to mock the Transaction and Result for the 'exists' check
              TransactionContext tx = mock(TransactionContext.class);
              when(tx.run(contains("gds.graph.exists"))).thenReturn(result);
              when(result.single()).thenReturn(record);
              when(record.get("exists")).thenReturn(value);
              when(value.asBoolean()).thenReturn(false);
              return cb.execute(tx);
            })
        .thenAnswer(
            invocation -> {
              // Second read: execute algorithm
              return List.of();
            });

    graphDAO.pageRankAlg(0, 10);

    // Verify write transaction (projection creation) was called
    verify(session, times(1)).executeWriteWithoutResult(any());
  }

  @Test
  void pageRankAlg_GraphExists_SkipsProjection() {
    when(driver.session()).thenReturn(session);

    // First read: check exists -> returns true
    when(session.executeRead(any()))
        .thenAnswer(
            invocation -> {
              TransactionCallback cb = invocation.getArgument(0);
              TransactionContext tx = mock(TransactionContext.class);
              when(tx.run(contains("gds.graph.exists"))).thenReturn(result);
              when(result.single()).thenReturn(record);
              when(record.get("exists")).thenReturn(value);
              when(value.asBoolean()).thenReturn(true);
              return cb.execute(tx);
            })
        .thenAnswer(
            invocation -> {
              return List.of();
            });

    graphDAO.pageRankAlg(0, 10);

    // Verify write transaction was NEVER called
    verify(session, never()).executeWriteWithoutResult(any());
  }
}
