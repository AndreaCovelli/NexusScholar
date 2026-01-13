package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.Paper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class PaperDAOIntegrationTest {

  @Autowired private PaperDAO paperDAO;
  @Autowired private MongoTemplate mongoTemplate;

  @BeforeEach
  void setUp() {
    mongoTemplate.dropCollection("papers");

    // Replicate the text index creation from 5_create_index.sh
    TextIndexDefinition textIndex =
        new TextIndexDefinition.TextIndexDefinitionBuilder()
            .onField("title", 10F)
            .onField("abstract", 3F)
            .build();
    mongoTemplate.indexOps("papers").createIndex(textIndex);

    // Seed data
    Paper p1 = new Paper();
    p1.setId("p1");
    p1.setTitle("Deep Learning for Vision");
    p1.setAbstractText("A comprehensive survey on CNNs.");
    p1.setYear(2023);
    p1.setDoi("10.1234/vision");

    Paper p2 = new Paper();
    p2.setId("p2");
    p2.setTitle("Natural Language Processing");
    p2.setAbstractText("Using Transformers for text generation.");
    p2.setYear(2023);
    p2.setDoi("10.1234/nlp");

    Paper p3 = new Paper();
    p3.setId("p3");
    p3.setTitle("Legacy AI Systems");
    p3.setAbstractText("Rule based systems.");
    p3.setYear(2015);
    p3.setDoi("10.1234/legacy");

    paperDAO.saveAll(List.of(p1, p2, p3));
  }

  @Test
  void findByDoi_Found() {
    Optional<Paper> paper = paperDAO.findByDoi("10.1234/nlp");
    assertTrue(paper.isPresent());
    assertEquals("Natural Language Processing", paper.get().getTitle());
  }

  @Test
  void findByTitleContainingIgnoreCase_Found() {
    Page<Paper> page = paperDAO.findByTitleContainingIgnoreCase("learning", PageRequest.of(0, 10));
    assertEquals(1, page.getTotalElements());
    assertEquals("Deep Learning for Vision", page.getContent().get(0).getTitle());
  }

  @Test
  void findByYear_Found() {
    Page<Paper> page = paperDAO.findByYear(2023, PageRequest.of(0, 10));
    assertEquals(2, page.getTotalElements());
  }

  @Test
  void findByTextSearch_RankedResults() {
    // Search for "Transformers" which appears in p2's abstract
    Page<Paper> page = paperDAO.findByTextSearch("Transformers", PageRequest.of(0, 10));

    assertFalse(page.isEmpty());
    assertEquals("Natural Language Processing", page.getContent().get(0).getTitle());
  }
}
