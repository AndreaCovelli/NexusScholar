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
    return new MongoDBContainer(DockerImageName.parse("mongo:latest"));
  }

  @Bean
  @ServiceConnection
  public Neo4jContainer<?> neo4jContainer() {
    // Add GDS plugin required for PageRank
    return new Neo4jContainer<>(DockerImageName.parse("neo4j:5.15.0"))
        .withLabsPlugins(Neo4jLabsPlugin.GRAPH_DATA_SCIENCE);
  }
}
