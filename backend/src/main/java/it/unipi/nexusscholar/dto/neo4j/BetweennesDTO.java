package it.unipi.nexusscholar.dto.neo4j;

import it.unipi.nexusscholar.utils.BetweennessEntry;

public class BetweennesDTO {

    private String paperTitle;
    private double score;

    public String getPaperTitle() {
        return paperTitle;
    }

    public void setPaperTitle(String paperTitle) {
        this.paperTitle= paperTitle;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
