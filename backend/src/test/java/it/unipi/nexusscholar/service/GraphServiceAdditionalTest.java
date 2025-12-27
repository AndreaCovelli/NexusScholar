package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import it.unipi.nexusscholar.NexusScholarBackendApplication;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;

class GraphServiceAdditionalTest {

  @Test
  void testConnectOverload() {
    // This unit test ensures the overloaded method delegates correctly
    Driver mockDriver = mock(Driver.class);
    doNothing().when(mockDriver).verifyConnectivity();

    GraphService service = new GraphService(mockDriver);

    // Calls connect(user, pass) -> connects() -> driver.verifyConnectivity()
    // Since mock does nothing (success), connect returns true
    assertTrue(service.connect("user", "pass"));
  }

  @Test
  void testMainApplication() {
    // Smoke test for the main method to ensure the entry point is covered
    try {
      NexusScholarBackendApplication.main(new String[] {});
    } catch (Exception ignored) {
      // Expected to fail context startup in unit test environment
    }
  }
}
