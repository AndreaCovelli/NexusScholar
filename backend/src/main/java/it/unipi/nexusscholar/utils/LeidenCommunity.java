package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class LeidenCommunity {

  private int communityId;
  private List<String> authors;

  public LeidenCommunity(int communityId, List<String> authors) {
    this.communityId = communityId;
    this.authors = authors;
  }

  public LeidenCommunity(int communityId) {
    this.communityId = communityId;
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
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      return "{}";
    }
  }
}
