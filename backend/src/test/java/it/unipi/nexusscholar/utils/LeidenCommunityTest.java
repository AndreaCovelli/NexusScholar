package it.unipi.nexusscholar.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeidenCommunityTest {

  @Test
  void testConstructorWithId() {
    LeidenCommunity community = new LeidenCommunity(5);

    assertEquals(5, community.getCommunityId());
    assertNotNull(community.getAuthors());
    assertTrue(community.getAuthors().isEmpty());
  }

  @Test
  void testConstructorWithIdAndAuthors() {
    List<String> authors = new ArrayList<>(Arrays.asList("Alice", "Bob"));
    LeidenCommunity community = new LeidenCommunity(10, authors);

    assertEquals(10, community.getCommunityId());
    assertEquals(2, community.getAuthors().size());
    assertTrue(community.getAuthors().contains("Alice"));
    assertTrue(community.getAuthors().contains("Bob"));
  }

  @Test
  void testAddAuthor() {
    LeidenCommunity community = new LeidenCommunity(1);

    community.addAuthor("Charlie");
    community.addAuthor("Diana");

    assertEquals(2, community.getAuthors().size());
    assertEquals("Charlie", community.getAuthors().get(0));
    assertEquals("Diana", community.getAuthors().get(1));
  }

  @Test
  void testToJsonWithEmptyAuthors() {
    LeidenCommunity community = new LeidenCommunity(3);

    String json = community.toJson();

    assertTrue(json.contains("\"communityId\":3"));
    assertTrue(json.contains("\"authors\":[]"));
  }

  @Test
  void testToJsonWithAuthors() {
    LeidenCommunity community = new LeidenCommunity(7);
    community.addAuthor("Eve");
    community.addAuthor("Frank");

    String json = community.toJson();

    assertTrue(json.contains("\"communityId\":7"));
    assertTrue(json.contains("\"authors\":[\"Eve\",\"Frank\"]"));
  }

  @Test
  void testGetCommunityId() {
    LeidenCommunity community = new LeidenCommunity(42);

    assertEquals(42, community.getCommunityId());
  }

  @Test
  void testGetAuthorsReturnsMutableList() {
    LeidenCommunity community = new LeidenCommunity(1);
    community.addAuthor("Test");

    List<String> authors = community.getAuthors();
    authors.add("Added");

    assertEquals(2, community.getAuthors().size());
  }
}
