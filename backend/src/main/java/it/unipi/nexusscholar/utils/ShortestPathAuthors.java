package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Path;

public class ShortestPathAuthors {

  private Path shortestPath;
  private int degreeSeparation;

  public ShortestPathAuthors(Path shortestPath, int degreeSeparation) {
    this.shortestPath = shortestPath;
    this.degreeSeparation = degreeSeparation;
  }

  public ShortestPathAuthors(Record r) {
    this.shortestPath = r.get("path").asPath();
    this.degreeSeparation = r.get("DegreeSeparation").asInt();
  }

  public String toJson() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module =
          new SimpleModule("ShortestPathSerializer", new Version(1, 0, 0, null, null, null));
      module.addSerializer(ShortestPathAuthors.class, new ShortestPathSerializer());
      mapper.registerModule(module);
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize ShortestPathAuthors to JSON", e);
    }
  }

  public Path getShortestPath() {
    return shortestPath;
  }

  public void setShortestPath(Path shortestPath) {
    this.shortestPath = shortestPath;
  }

  public int getDegreeSeparation() {
    return degreeSeparation;
  }

  public void setDegreeSeparation(int degreeSeparation) {
    this.degreeSeparation = degreeSeparation;
  }
}
