package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import it.unipi.nexusscholar.dao.RegisteredUserAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisteredUserAnalysisServiceImplTest {

  @Mock private RegisteredUserAnalysisDAO dao;
  @InjectMocks private RegisteredUserAnalysisServiceImpl service;

  @Test
  void getPaperLeaderboard_Success() {
    PaperLeaderboard entry = new PaperLeaderboard("p1", "Top Paper", 100);
    when(dao.getMostBookmarkedPapers(2023, 10)).thenReturn(List.of(entry));

    List<PaperLeaderboardDTO> result = service.getPaperLeaderboard(2023, 10);

    assertEquals(1, result.size());
    assertEquals("p1", result.get(0).getPaperId());
    assertEquals(100, result.get(0).getBookmarkedReceived());
  }
}
