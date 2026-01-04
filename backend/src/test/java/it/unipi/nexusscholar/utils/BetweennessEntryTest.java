package it.unipi.nexusscholar.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

class BetweennessEntryTest {

  @Test
  void testConstructorWithParameters() {
    BetweennessEntry entry = new BetweennessEntry("Alice Chen", 42.5);
    assertEquals("Alice Chen", entry.getAuthorName());
    assertEquals(42.5, entry.getScore());
  }

  @Test
  void testConstructorFromRecord() {
    // Mock Neo4j Record
    Record mockRecord = mock(Record.class);
    Value mockName = mock(Value.class);
    Value mockScore = mock(Value.class);

    when(mockRecord.get("name")).thenReturn(mockName);
    when(mockRecord.get("score")).thenReturn(mockScore);
    when(mockName.asString()).thenReturn("Bob Martinez");
    when(mockScore.asDouble()).thenReturn(127.3);

    BetweennessEntry entry = new BetweennessEntry(mockRecord);
    assertEquals("Bob Martinez", entry.getAuthorName());
    assertEquals(127.3, entry.getScore());
  }

  @Test
  void testSetters() {
    BetweennessEntry entry = new BetweennessEntry();
    entry.setAuthorName("Carol Yamamoto");
    entry.setScore(99.9);

    assertEquals("Carol Yamamoto", entry.getAuthorName());
    assertEquals(99.9, entry.getScore());
  }

  @Test
  void testJsonSerialization() {
    BetweennessEntry entry = new BetweennessEntry("David Kim", 15.75);
    String json = entry.toJson();

    assertEquals("{\"authorName\":\"David Kim\",\"score\":15.75}", json);
  }
}
