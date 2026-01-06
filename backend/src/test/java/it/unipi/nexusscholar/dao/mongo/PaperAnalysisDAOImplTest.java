package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.Paper;
import it.unipi.nexusscholar.model.mongo.PaperAuthor;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PaperAnalysisDAOImplTest {

  @Autowired private PaperAnalysisDAOImpl paperAnalysisDAO;
  @Autowired private MongoTemplate mongoTemplate;

  @BeforeEach
  void setUp() {
    mongoTemplate.dropCollection("papers");
  }

  @Test
  void getTrendAnalysis_ReturnsResults() {
    Paper paper1 =
        createPaper("P001", "ML Paper", 2023, List.of("Machine Learning"), List.of("NeurIPS"));
    Paper paper2 = createPaper("P002", "DL Paper", 2023, List.of("Deep Learning"), List.of("ICML"));
    Paper paper3 =
        createPaper("P003", "ML Paper 2", 2022, List.of("Machine Learning"), List.of("NeurIPS"));

    mongoTemplate.save(paper1);
    mongoTemplate.save(paper2);
    mongoTemplate.save(paper3);

    List<TrendAnalysis> results = paperAnalysisDAO.getTrendAnalysis();

    assertEquals(3, results.size());
    assertTrue(
        results.stream()
            .anyMatch(
                r ->
                    r.getFieldOfStudy().equals("Machine Learning")
                        && r.getYear() == 2023
                        && r.getPaperCreated() == 1));
    assertTrue(
        results.stream()
            .anyMatch(
                r ->
                    r.getFieldOfStudy().equals("Deep Learning")
                        && r.getYear() == 2023
                        && r.getPaperCreated() == 1));
    assertTrue(
        results.stream()
            .anyMatch(
                r ->
                    r.getFieldOfStudy().equals("Machine Learning")
                        && r.getYear() == 2022
                        && r.getPaperCreated() == 1));
  }

  @Test
  void getTrendAnalysis_EmptyCollection() {
    List<TrendAnalysis> results = paperAnalysisDAO.getTrendAnalysis();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void getVenueAnalysis_ReturnsResults() {
    Paper paper1 = createPaper("P001", "Paper 1", 2023, List.of("AI"), List.of("NeurIPS"));
    Paper paper2 = createPaper("P002", "Paper 2", 2023, List.of("AI"), List.of("NeurIPS"));
    Paper paper3 = createPaper("P003", "Paper 3", 2023, List.of("AI"), List.of("ICML"));

    mongoTemplate.save(paper1);
    mongoTemplate.save(paper2);
    mongoTemplate.save(paper3);

    List<VenueAnalysis> results = paperAnalysisDAO.getVenueAnalysis();

    assertEquals(2, results.size());
    assertEquals("NeurIPS", results.get(0).getVenue());
    assertEquals(2, results.get(0).getPaperCreated());
    assertEquals("ICML", results.get(1).getVenue());
    assertEquals(1, results.get(1).getPaperCreated());
  }

  @Test
  void getVenueAnalysis_EmptyCollection() {
    List<VenueAnalysis> results = paperAnalysisDAO.getVenueAnalysis();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void getCollaborationEvolution_ReturnsResults() {
    Paper paper1 = createPaper("P001", "Paper 1", 2023, List.of("AI"), List.of("NeurIPS"));
    paper1.setAuthors(
        List.of(
            new PaperAuthor("A1", "Author 1"),
            new PaperAuthor("A2", "Author 2"),
            new PaperAuthor("A3", "Author 3")));

    Paper paper2 = createPaper("P002", "Paper 2", 2022, List.of("AI"), List.of("ICML"));
    paper2.setAuthors(
        List.of(new PaperAuthor("A4", "Author 4"), new PaperAuthor("A5", "Author 5")));

    mongoTemplate.save(paper1);
    mongoTemplate.save(paper2);

    List<CollaborationEvolution> results = paperAnalysisDAO.getCollaborationEvolution();

    assertEquals(2, results.size());
    assertEquals(2022, results.get(0).getYear());
    assertEquals(2.0, results.get(0).getAvgAuthors(), 0.001);
    assertEquals(2023, results.get(1).getYear());
    assertEquals(3.0, results.get(1).getAvgAuthors(), 0.001);
  }

  @Test
  void getCollaborationEvolution_EmptyCollection() {
    List<CollaborationEvolution> results = paperAnalysisDAO.getCollaborationEvolution();

    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  private Paper createPaper(
      String id, String title, int year, List<String> fields, List<String> venues) {
    Paper paper = new Paper();
    paper.setId(id);
    paper.setTitle(title);
    paper.setYear(year);
    paper.setFieldsOfStudy(fields);
    paper.setVenue(venues);
    paper.setAuthors(List.of());
    return paper;
  }
}
