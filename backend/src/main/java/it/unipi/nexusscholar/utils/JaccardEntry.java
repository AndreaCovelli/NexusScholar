package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;

public class JaccardEntry {

  private String author1;
  private String author2;
  private double similarity;

  public JaccardEntry(String author1, String author2, double similarity) {
    this.author1 = author1;
    this.author2 = author2;
    this.similarity = similarity;
  }

  public JaccardEntry() {
    this.author1 = null;
    this.author2 = null;
    this.similarity = 0;
  }

  public JaccardEntry(Record r) {
    this.author1 = r.get("author1").asString();
    this.author2 = r.get("author2").asString();
    this.similarity = r.get("similarity").asDouble();
  }

  public String getAuthor1() {
    return author1;
  }

  public void setAuthor1(String author1) {
    this.author1 = author1;
  }

  public String getAuthor2() {
    return author2;
  }

  public void setAuthor2(String author2) {
    this.author2 = author2;
  }

  public double getSimilarity() {
    return similarity;
  }

  public void setSimilarity(double similarity) {
    this.similarity = similarity;
  }

  public String toJson() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize JaccardEntry to JSON", e);
    }
  }
}
