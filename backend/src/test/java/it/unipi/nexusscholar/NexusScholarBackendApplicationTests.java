package it.unipi.nexusscholar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test") // Add this to use test profile
class NexusScholarBackendApplicationTests {

  @Test
  void contextLoads() {
    // Context loads successfully if this test passes
  }
}
