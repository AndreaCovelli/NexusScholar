package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.Record;

public class PageRankEntry {

  private String paperTitle;
  private double rank;

  public PageRankEntry(double rank, String paperTitle) {
    this.rank = rank;
    this.paperTitle = paperTitle;
  }

  public PageRankEntry(Record r) {
    this.rank = r.get("rank").asDouble();
    this.paperTitle = r.get("paperTitle").asString();
  }

  public String toJson() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize PageRankEntry to JSON", e);
    }
  }

  public String getPaperTitle() {
    return paperTitle;
  }

  public void setPaperTitle(String paperTitle) {
    this.paperTitle = paperTitle;
  }

  public double getRank() {
    return rank;
  }

  public void setRank(double rank) {
    this.rank = rank;
  }
}
