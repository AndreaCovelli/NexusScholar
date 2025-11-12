package it.unipi.nexusscholar;

import org.springframework.boot.SpringApplication;

public class TestNexusScholarBackendApplication {

  public static void main(String[] args) {
    SpringApplication.from(NexusScholarBackendApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
