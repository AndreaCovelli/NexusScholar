package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.BookmarkedPaper;
import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RegisteredUserAnalysisDAOImplTest {

  @Autowired private RegisteredUserAnalysisDAOImpl dao;
  @Autowired private MongoTemplate mongoTemplate;

  @BeforeEach
  void setUp() {
    mongoTemplate.dropCollection("registeredUsers");
  }

  @Test
  void getMostBookmarkedPapers_ReturnsCorrectCounts() {
    // 1. Setup Data
    LocalDateTime targetDate = LocalDateTime.of(2025, 5, 15, 10, 0);

    BookmarkedPaper p1 = new BookmarkedPaper("p1", "Paper 1", targetDate);
    BookmarkedPaper p2 = new BookmarkedPaper("p2", "Paper 2", targetDate);

    RegisteredUser u1 = new RegisteredUser();
    u1.setBookmarkedPapers(List.of(p1, p2));

    RegisteredUser u2 = new RegisteredUser();
    u2.setBookmarkedPapers(List.of(p1)); // p1 gets 2 votes, p2 gets 1

    mongoTemplate.save(u1);
    mongoTemplate.save(u2);

    // 2. Execute
    List<PaperLeaderboard> result = dao.getMostBookmarkedPapers(2025, 5);

    // 3. Verify
    assertEquals(2, result.size());
    assertEquals("p1", result.get(0).getPaperId());
    assertEquals(2, result.get(0).getBookmarkedReceived());
    assertEquals("p2", result.get(1).getPaperId());
    assertEquals(1, result.get(1).getBookmarkedReceived());
  }
}
