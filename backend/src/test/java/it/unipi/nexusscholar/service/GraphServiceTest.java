package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class GraphServiceTest {

  @Autowired private GraphService graphService;
  @Autowired private Driver driver;

  @BeforeEach
  void setup() {
    try (Session session = driver.session()) {
      // Drop the GDS graph projection if it exists from a previous run to ensure a clean state
      session.run("CALL gds.graph.drop('paperCitations', false)");

      // Clear the database
      session.run("MATCH (n) DETACH DELETE n");

      // Create test data including the required CITES relationship
      session.run(
          """
                  CREATE (a1:Author {name: 'Jennifer Dean'})
                  CREATE (a2:Author {name: 'B. Rappazzo'})
                  CREATE (p1:Paper {title: 'Test Paper 1'})
                  CREATE (p2:Paper {title: 'Test Paper 2'})
                  MERGE (a1)-[:AUTHORED]->(p1)
                  MERGE (a2)-[:AUTHORED]->(p1)
                  MERGE (p1)-[:CITES]->(p2)
                  """);
    }
  }

  @Test
  public void TestGraphService() {
    assertTrue(graphService.connect());
    assertNotNull(graphService.pagerank(PageRequest.of(0, 20)));
    assertNotNull(graphService.collabPath("Jennifer Dean", "B. Rappazzo"));
    assertNull(graphService.collabPath("Chunhong Pan", "DanieleCong"));
    assertNotNull(graphService.hiddenCommunities(PageRequest.of(0, 20)));
    assertNotNull(graphService.betweenness(PageRequest.of(0, 20)));
  }
}
