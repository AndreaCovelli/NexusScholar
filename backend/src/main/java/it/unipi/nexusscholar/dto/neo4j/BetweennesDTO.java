package it.unipi.nexusscholar.dto.neo4j;

import it.unipi.nexusscholar.utils.BetweennessEntry;

public class BetweennesDTO {

    private String authorName;
    private double score;

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
