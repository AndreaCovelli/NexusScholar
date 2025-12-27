package it.unipi.nexusscholar.utils;

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
    return "{\"DegreeSeparation\":"
        + this.degreeSeparation
        + "\",path\":\""
        + this.shortestPath.toString()
        + "\"}";
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
