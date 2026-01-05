package it.unipi.nexusscholar.dto.neo4j;

public class BetweennesDTO {

  private String paperTitle;
  private double betweenness;

  public String getPaperTitle() {
    return paperTitle;
  }

  public void setPaperTitle(String paperTitle) {
    this.paperTitle = paperTitle;
  }

  public double getBetweenness() {
    return betweenness;
  }

  public void setBetweenness(double betweenness) {
    this.betweenness = betweenness;
  }
}
