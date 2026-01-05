package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.Objects;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

/**
 * Represents a betweenness centrality score for a paper node.
 *
 * <p>Betweenness centrality quantifies how often a node lies on the shortest path between other
 * nodes. In citation networks, papers with high betweenness act as critical bridges connecting
 * different research communities.
 */
public class BetweennessEntry implements Comparable<BetweennessEntry> {

  private String paperId;
  private String title;
  private double betweenness;

  /** Default constructor for serialization frameworks. */
  public BetweennessEntry() {}

  /**
   * Constructs a BetweennessEntry with explicit values.
   *
   * @param paperId the internal paper identifier
   * @param title the paper title
   * @param betweenness the betweenness centrality score
   */
  public BetweennessEntry(String paperId, String title, double betweenness) {
    this.paperId = paperId;
    this.title = title;
    this.betweenness = betweenness;
  }

  /**
   * Constructs a BetweennessEntry with title and score only.
   *
   * @param title the paper title
   * @param betweenness the betweenness centrality score
   */
  public BetweennessEntry(String title, double betweenness) {
    this.paperId = null;
    this.title = title;
    this.betweenness = betweenness;
  }

  /**
   * Constructs a BetweennessEntry from a Neo4j Record.
   *
   * <p>Expected record fields:
   *
   * <ul>
   *   <li>{@code paperId} (optional): Internal paper ID
   *   <li>{@code title}: Paper title
   *   <li>{@code betweenness}: Betweenness centrality score
   * </ul>
   *
   * @param record the Neo4j record from a GDS betweenness query
   * @throws IllegalArgumentException if required fields are missing
   */
  public BetweennessEntry(Record record) {
    Objects.requireNonNull(record, "Record cannot be null");

    // Extract title (required)
    Value titleValue = record.get("title");
    if (titleValue.isNull()) {
      throw new IllegalArgumentException("Record must contain 'title' field");
    }
    this.title = titleValue.asString();

    // Extract betweenness score (required)
    Value betweennessValue = record.get("betweenness");
    if (betweennessValue.isNull()) {
      throw new IllegalArgumentException("Record must contain 'betweenness' field");
    }
    this.betweenness = betweennessValue.asDouble();

    // Extract paperId (optional)
    Value paperIdValue = record.get("paperId");
    this.paperId = paperIdValue.isNull() ? null : paperIdValue.asString();
  }

  /**
   * Serializes this entry to a JSON string.
   *
   * @return JSON representation of this entry
   * @throws RuntimeException if serialization fails
   */
  public String toJson() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize BetweennessEntry to JSON", e);
    }
  }

  /**
   * Returns a normalized betweenness score in the range [0, 1].
   *
   * <p>Normalization uses the formula: score / ((n-1)(n-2)/2) for undirected graphs, where n is the
   * total node count. This method requires the total node count to be provided.
   *
   * @param totalNodes the total number of nodes in the graph
   * @return normalized betweenness score
   * @throws IllegalArgumentException if totalNodes is less than 3
   */
  @JsonIgnore
  public double getNormalizedBetweenness(long totalNodes) {
    if (totalNodes < 3) {
      throw new IllegalArgumentException("Graph must have at least 3 nodes for normalization");
    }
    double maxBetweenness = ((totalNodes - 1.0) * (totalNodes - 2.0)) / 2.0;
    return betweenness / maxBetweenness;
  }

  /**
   * Determines if this paper is a significant bridge in the citation network.
   *
   * <p>A paper is considered a bridge if its betweenness score exceeds the specified threshold.
   * Typical thresholds range from 100 to 10000 depending on network size.
   *
   * @param threshold the minimum betweenness score to qualify as a bridge
   * @return true if betweenness exceeds threshold
   */
  @JsonIgnore
  public boolean isBridge(double threshold) {
    return betweenness >= threshold;
  }

  @Override
  public int compareTo(BetweennessEntry other) {
    // Descending order: higher betweenness first
    return Double.compare(other.betweenness, this.betweenness);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BetweennessEntry that = (BetweennessEntry) o;
    return Double.compare(that.betweenness, betweenness) == 0
        && Objects.equals(paperId, that.paperId)
        && Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paperId, title, betweenness);
  }

  @Override
  public String toString() {
    return String.format(
        Locale.US,
        "BetweennessEntry{paperId='%s', title='%s', betweenness=%.4f}",
        paperId,
        title,
        betweenness);
  }

  // ===== Getters and Setters =====

  @JsonProperty("paperId")
  public String getPaperId() {
    return paperId;
  }

  public void setPaperId(String paperId) {
    this.paperId = paperId;
  }

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("betweenness")
  public double getBetweenness() {
    return betweenness;
  }

  public void setBetweenness(double betweenness) {
    this.betweenness = betweenness;
  }
}
