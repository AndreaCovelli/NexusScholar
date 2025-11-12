package it.unipi.nexusscholar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// This is a pure Unit Test (no @SpringBootTest), so it runs very fast.
public class HealthServiceTest {

  private final HealthService healthService = new HealthService();

  @Test
  public void testGetStatus() {
    String status = healthService.getStatus();
    assertEquals("Active", status, "The health status should be Active");
  }
}
