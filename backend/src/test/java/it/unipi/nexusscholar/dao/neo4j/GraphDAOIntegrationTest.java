package it.unipi.nexusscholar.dao.neo4j;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.dto.mongo.PaperAuthorDTO;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class GraphDAOIntegrationTest {

  @Autowired private GraphDAO graphDAO;
  @Autowired private Driver driver;

  @BeforeEach
  void setUp() {
    try (Session session = driver.session()) {
      session.run("MATCH (n) DETACH DELETE n");
    }
  }

  @Test
  void savePaperNode_CreatesNodesAndRelationships() {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setId("P100");
    paperDTO.setTitle("Integration Test Paper");

    PaperAuthorDTO author1 = new PaperAuthorDTO("A1", "Test Author 1");
    PaperAuthorDTO author2 = new PaperAuthorDTO("A2", "Test Author 2");
    paperDTO.setAuthors(List.of(author1, author2));

    boolean success = graphDAO.savePaperNode(paperDTO);
    assertTrue(success);

    try (Session session = driver.session()) {
      // Verify Paper Node
      Result resPaper = session.run("MATCH (p:Paper {paperID: 'P100'}) RETURN p.title as title");
      assertTrue(resPaper.hasNext());
      assertEquals("Integration Test Paper", resPaper.single().get("title").asString());

      // Verify Author Nodes
      Result resAuthors = session.run("MATCH (a:Author) RETURN count(a) as count");
      assertEquals(2, resAuthors.single().get("count").asInt());

      // Verify Relationships
      Result resRel =
          session.run(
              "MATCH (:Author)-[r:AUTHORED]->(:Paper {paperID: 'P100'}) RETURN count(r) as count");
      assertEquals(2, resRel.single().get("count").asInt());
    }
  }

  @Test
  void savePaperNode_UpdatesExistingPaper() {
    // 1. Save initial version
    PaperDTO v1 = new PaperDTO();
    v1.setId("P200");
    v1.setTitle("Old Title");
    v1.setAuthors(List.of(new PaperAuthorDTO("A1", "Author One")));
    graphDAO.savePaperNode(v1);

    // 2. Save updated version (New title, different author)
    PaperDTO v2 = new PaperDTO();
    v2.setId("P200");
    v2.setTitle("New Title");
    v2.setAuthors(List.of(new PaperAuthorDTO("A2", "Author Two")));
    graphDAO.savePaperNode(v2);

    try (Session session = driver.session()) {
      // Verify Title Update
      Result resPaper = session.run("MATCH (p:Paper {paperID: 'P200'}) RETURN p.title as title");
      assertEquals("New Title", resPaper.single().get("title").asString());

      // Verify Relationship Update (Old author should be removed from this paper, new one added)
      Result resRel =
          session.run(
              "MATCH (a:Author)-[:AUTHORED]->(:Paper {paperID: 'P200'}) RETURN a.authorId as id");
      assertTrue(resRel.hasNext());
      assertEquals("A2", resRel.single().get("id").asString());
    }
  }
}
