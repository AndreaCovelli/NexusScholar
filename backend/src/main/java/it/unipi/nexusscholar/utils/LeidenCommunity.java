package it.unipi.nexusscholar.utils;

import java.util.ArrayList;
import java.util.List;

public class LeidenCommunity {

  private int communityId;
  private List<String> authors;

  public LeidenCommunity(int communityId, List<String> authors) {
    this.communityId = communityId;
    this.authors = authors;
  }

  public LeidenCommunity(int i) {
    this.communityId = i;
    this.authors = new ArrayList<>();
  }

  public int getCommunityId() {
    return communityId;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public void addAuthor(String author) {
    authors.add(author);
  }

  public String toJson() {
    String json = "{\"communityId\":" + communityId + ",\"authors\":[";
    int countAuthors = 0, sizeAuthors = authors.size();
    for (String author : authors) {
      json += "\"" + author + "\"";
      if (countAuthors != sizeAuthors) {
        json += ",";
        countAuthors++;
      }
    }
    json += "]}";
    return json;
  }
}
