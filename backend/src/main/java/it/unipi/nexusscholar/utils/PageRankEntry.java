package it.unipi.nexusscholar.utils;

import org.neo4j.driver.Record;

public class PageRankEntry {

    private String paperTitle;
    private double rank;

    public PageRankEntry(double rank, String paperTitle) {
        this.rank = rank;
        this.paperTitle = paperTitle;
    }

    public PageRankEntry(Record r){
        this.rank = r.get("rank").asDouble();
        this.paperTitle = r.get("paperTitle").toString();
    }

    public String toJson(){
        return "{\"title\":\""+this.paperTitle+"\",\"rank\":"+this.rank+"}";
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
