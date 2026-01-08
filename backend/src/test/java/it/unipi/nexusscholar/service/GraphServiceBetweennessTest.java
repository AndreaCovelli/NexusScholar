package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class GraphServiceBetweennessTest {

  @Autowired private GraphService graphService;
  @Autowired private Driver driver;

  @BeforeEach
  void setup() {
    try (Session session = driver.session()) {
      session.run("CALL gds.graph.drop('paperCitations', false)");
      session.run("MATCH (n) DETACH DELETE n");

      session.run(
          """
                CREATE (p1:Paper {title: 'Hub Paper'})
                CREATE (p2:Paper {title: 'Citing Paper 1'})
                CREATE (p3:Paper {title: 'Citing Paper 2'})
                CREATE (p4:Paper {title: 'Citing Paper 3'})
                CREATE (p5:Paper {title: 'Foundation Paper'})
                CREATE (p2)-[:CITES]->(p1)
                CREATE (p3)-[:CITES]->(p1)
                CREATE (p4)-[:CITES]->(p1)
                CREATE (p1)-[:CITES]->(p5)
            """);
    }
  }

  @Test
  void testBetweennessCalculation() {
    Page<BetweennessDTO> page = graphService.betweenness(PageRequest.of(0, 10));
    List<BetweennessDTO> results = page.getContent();

    assertNotNull(results, "Betweenness results should not be null");
    assertFalse(results.isEmpty(), "Should return at least one paper");

    assertEquals(
        "Hub Paper", results.get(0).getPaperTitle(), "Hub paper should have highest betweenness");
  }
}
