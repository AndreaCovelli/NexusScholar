package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.utils.BetweennessEntry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class GraphServiceBetweennessTest {

  @Autowired private GraphService graphService;
  @Autowired private Driver driver;

  @BeforeEach
  void setup() {
    try (Session session = driver.session()) {
      // Clean slate
      session.run("CALL gds.graph.drop('coAuthors', false)");
      session.run("MATCH (n) DETACH DELETE n");

      // Create a simple collaboration network:
      //   A1 ---[Paper1]--- A2 ---[Paper2]--- A3
      //   (A2 is the bridge between A1 and A3)
      session.run(
          """
                    CREATE (a1:Author {name: 'Alice'})
                    CREATE (a2:Author {name: 'Bob'})
                    CREATE (a3:Author {name: 'Carol'})
                    CREATE (p1:Paper {title: 'Paper 1'})
                    CREATE (p2:Paper {title: 'Paper 2'})
                    MERGE (a1)-[:AUTHORED]->(p1)
                    MERGE (a2)-[:AUTHORED]->(p1)
                    MERGE (a2)-[:AUTHORED]->(p2)
                    MERGE (a3)-[:AUTHORED]->(p2)
                    """);
    }
  }

  @Test
  void testBetweennessCalculation() {
    List<BetweennessEntry> results = graphService.betweenness();

    assertNotNull(results, "Betweenness results should not be null");
    assertFalse(results.isEmpty(), "Should return at least one author");

    // Verify Bob (the bridge) has the highest score
    BetweennessEntry topAuthor = results.get(0);
    assertEquals("Bob", topAuthor.getAuthorName(), "Bob should be the top bridge author");
    assertEquals(1.0, topAuthor.getScore(), "Bridge author's betweenness score should be 1.0");
  }

  @Test
  void testBetweennessReusesGraphProjection() {
    // First call creates the projection
    List<BetweennessEntry> firstRun = graphService.betweenness();
    assertNotNull(firstRun);

    // Second call should reuse the existing projection (no errors)
    List<BetweennessEntry> secondRun = graphService.betweenness();
    assertNotNull(secondRun);
    assertEquals(firstRun.size(), secondRun.size(), "Results should be consistent across runs");
  }
}
