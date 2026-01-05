package it.unipi.nexusscholar.dto.neo4j;

public class PageRankDTO {

    private String paperTitle;
    private double rank;

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
