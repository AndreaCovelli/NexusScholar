package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;

public class BetweennessEntry {
  private String authorName;
  private double score;

  // No-arg constructor for Jackson deserialization (testing purposes)
  public BetweennessEntry() {}

  public BetweennessEntry(String authorName, double score) {
    this.authorName = authorName;
    this.score = score;
  }

  public BetweennessEntry(Record r) {
    this.authorName = r.get("name").asString();
    this.score = r.get("score").asDouble();
  }

  public String toJson() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize BetweennessEntry to JSON", e);
    }
  }

  // Getters and setters for proper JavaBean compliance
  public String getAuthorName() {
    return authorName;
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }
}
