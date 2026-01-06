package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.Author;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import it.unipi.nexusscholar.model.mongo.PublicationSummary;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthorAnalysisDAOImplTest {

  @Autowired private AuthorAnalysisDAOImpl authorAnalysisDAO;
  @Autowired private MongoTemplate mongoTemplate;

  @BeforeEach
  void setUp() {
    mongoTemplate.dropCollection("authors");
  }

  @Test
  void getProlificAuthors_ReturnsAuthorsWithMinPublications() {
    // Create author with 15 publications in 2023
    Author prolificAuthor = new Author();
    prolificAuthor.setId("A001");
    prolificAuthor.setName("Prolific Researcher");
    prolificAuthor.setS2AuthorId("s2-prolific");
    prolificAuthor.setTotalPublications(15);

    List<PublicationSummary> publications = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      publications.add(new PublicationSummary("P" + i, 2023, "Paper " + i));
    }
    prolificAuthor.setPublicationsSummary(publications);

    // Create author with only 2 publications
    Author normalAuthor = new Author();
    normalAuthor.setId("A002");
    normalAuthor.setName("Normal Researcher");
    normalAuthor.setS2AuthorId("s2-normal");
    normalAuthor.setTotalPublications(2);
    normalAuthor.setPublicationsSummary(
        List.of(
            new PublicationSummary("P100", 2023, "Paper A"),
            new PublicationSummary("P101", 2023, "Paper B")));

    mongoTemplate.save(prolificAuthor);
    mongoTemplate.save(normalAuthor);

    List<ProlificAuthor> results = authorAnalysisDAO.getProlificAuthors(10);

    assertEquals(1, results.size());
    ProlificAuthor resultAuthor = results.get(0);
    assertEquals("A001", resultAuthor.getAuthorId());
    assertEquals("Prolific Researcher", resultAuthor.getAuthorName());
    // The aggregation groups by year, so we should get results
    // The exact count depends on aggregation logic
  }

  @Test
  void getProlificAuthors_EmptyCollection() {
    List<ProlificAuthor> results = authorAnalysisDAO.getProlificAuthors(5);

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }
}
