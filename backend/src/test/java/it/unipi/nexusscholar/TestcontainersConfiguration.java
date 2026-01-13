package it.unipi.nexusscholar;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  public MongoDBContainer mongoDbContainer() {
    MongoDBContainer container =
        new MongoDBContainer(DockerImageName.parse("mongo:8.2.3-noble"))
            .withReuse(true); // Enable container reuse for faster tests
    container.start();
    return container;
  }

  @Bean
  @ServiceConnection
  public Neo4jContainer<?> neo4jContainer() {
    Neo4jContainer<?> container =
        new Neo4jContainer<>(DockerImageName.parse("neo4j:2025.11.2-enterprise-bullseye"))
            .withLabsPlugins(Neo4jLabsPlugin.GRAPH_DATA_SCIENCE)
            .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes")
            .withReuse(true); // Enable container reuse
    container.start();
    return container;
  }
}
