package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GraphServiceTest {

  private final GraphService graphService = new GraphService();

  @Test
  public void TestGraphService() {
    GraphService graphService = new GraphService();
    // Test method connect to verify connection to neo4j istance
    assertTrue(graphService.connect("neo4j", "secretpassword"));
    // Pagerank test
    assertNotNull(graphService.pagerank());
    // shortestPath test
    assertNotNull(graphService.collabPath("Jennifer Dean", "B. Rappazzo"));
  }
}
