package it.unipi.nexusscholar.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

class PageRankEntryTest {

  @Test
  void testConstructorWithValues() {
    PageRankEntry entry = new PageRankEntry(0.95, "Important Paper");

    assertEquals(0.95, entry.getRank(), 0.001);
    assertEquals("Important Paper", entry.getPaperTitle());
  }

  @Test
  void testConstructorWithRecord() {
    Record mockRecord = mock(Record.class);
    Value mockRank = mock(Value.class);
    Value mockTitle = mock(Value.class);

    when(mockRecord.get("rank")).thenReturn(mockRank);
    when(mockRecord.get("title")).thenReturn(mockTitle);
    when(mockRank.asDouble()).thenReturn(0.75);
    when(mockTitle.asString()).thenReturn("Test Title");

    PageRankEntry entry = new PageRankEntry(mockRecord);

    assertEquals(0.75, entry.getRank(), 0.001);
    assertEquals("Test Title", entry.getPaperTitle());
  }

  @Test
  void testSetRank() {
    PageRankEntry entry = new PageRankEntry(0.5, "Paper");

    entry.setRank(0.99);

    assertEquals(0.99, entry.getRank(), 0.001);
  }

  @Test
  void testSetPaperTitle() {
    PageRankEntry entry = new PageRankEntry(0.5, "Old Title");

    entry.setPaperTitle("New Title");

    assertEquals("New Title", entry.getPaperTitle());
  }

  @Test
  void testToJson() {
    PageRankEntry entry = new PageRankEntry(0.85, "JSON Test Paper");

    String json = entry.toJson();

    assertTrue(json.contains("\"paperTitle\":\"JSON Test Paper\""));
    assertTrue(json.contains("\"rank\":0.85"));
  }

  @Test
  void testToJsonWithSpecialCharacters() {
    PageRankEntry entry = new PageRankEntry(0.5, "Paper with \"quotes\" and \\backslash");

    String json = entry.toJson();

    assertNotNull(json);
    assertTrue(json.contains("Paper with"));
  }
}
