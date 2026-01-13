package it.unipi.nexusscholar.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

class BetweennessEntryTest {

  @Test
  void testDefaultConstructor() {
    BetweennessEntry entry = new BetweennessEntry();
    assertNull(entry.getPaperId());
    assertNull(entry.getTitle());
    assertEquals(0.0, entry.getBetweenness());
  }

  @Test
  void testFullConstructor() {
    BetweennessEntry entry = new BetweennessEntry("P00001", "Deep Learning", 1523.45);

    assertEquals("P00001", entry.getPaperId());
    assertEquals("Deep Learning", entry.getTitle());
    assertEquals(1523.45, entry.getBetweenness(), 0.001);
  }

  @Test
  void testTwoArgConstructor() {
    BetweennessEntry entry = new BetweennessEntry("Graph Theory", 999.0);

    assertNull(entry.getPaperId());
    assertEquals("Graph Theory", entry.getTitle());
    assertEquals(999.0, entry.getBetweenness());
  }

  @Test
  void testRecordConstructor() {
    Record mockRecord = mock(Record.class);
    Value mockTitle = mock(Value.class);
    Value mockBetweenness = mock(Value.class);
    Value mockPaperId = mock(Value.class);

    when(mockRecord.get("title")).thenReturn(mockTitle);
    when(mockRecord.get("betweenness")).thenReturn(mockBetweenness);
    when(mockRecord.get("paperId")).thenReturn(mockPaperId);

    when(mockTitle.isNull()).thenReturn(false);
    when(mockTitle.asString()).thenReturn("Attention Is All You Need");
    when(mockBetweenness.isNull()).thenReturn(false);
    when(mockBetweenness.asDouble()).thenReturn(50432.89);
    when(mockPaperId.isNull()).thenReturn(false);
    when(mockPaperId.asString()).thenReturn("P12345");

    BetweennessEntry entry = new BetweennessEntry(mockRecord);

    assertEquals("P12345", entry.getPaperId());
    assertEquals("Attention Is All You Need", entry.getTitle());
    assertEquals(50432.89, entry.getBetweenness(), 0.001);
  }

  @Test
  void testRecordConstructorWithNullPaperId() {
    Record mockRecord = mock(Record.class);
    Value mockTitle = mock(Value.class);
    Value mockBetweenness = mock(Value.class);
    Value mockPaperId = mock(Value.class);

    when(mockRecord.get("title")).thenReturn(mockTitle);
    when(mockRecord.get("betweenness")).thenReturn(mockBetweenness);
    when(mockRecord.get("paperId")).thenReturn(mockPaperId);

    when(mockTitle.isNull()).thenReturn(false);
    when(mockTitle.asString()).thenReturn("Test Paper");
    when(mockBetweenness.isNull()).thenReturn(false);
    when(mockBetweenness.asDouble()).thenReturn(100.0);
    when(mockPaperId.isNull()).thenReturn(true);

    BetweennessEntry entry = new BetweennessEntry(mockRecord);

    assertNull(entry.getPaperId());
    assertEquals("Test Paper", entry.getTitle());
  }

  @Test
  void testRecordConstructorThrowsOnNullRecord() {
    assertThrows(NullPointerException.class, () -> new BetweennessEntry(null));
  }

  @Test
  void testRecordConstructorThrowsOnMissingTitle() {
    Record mockRecord = mock(Record.class);
    Value mockTitle = mock(Value.class);

    when(mockRecord.get("title")).thenReturn(mockTitle);
    when(mockTitle.isNull()).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> new BetweennessEntry(mockRecord));
  }

  @Test
  void testSetters() {
    BetweennessEntry entry = new BetweennessEntry();

    entry.setPaperId("P99999");
    entry.setTitle("Updated Title");
    entry.setBetweenness(777.77);

    assertEquals("P99999", entry.getPaperId());
    assertEquals("Updated Title", entry.getTitle());
    assertEquals(777.77, entry.getBetweenness());
  }

  @Test
  void testToJson() {
    BetweennessEntry entry = new BetweennessEntry("P00001", "Test Paper", 1234.56);

    String json = entry.toJson();

    assertTrue(json.contains("\"paperId\":\"P00001\""));
    assertTrue(json.contains("\"title\":\"Test Paper\""));
    assertTrue(json.contains("\"betweenness\":1234.56"));
  }

  @Test
  void testNormalizedBetweenness() {
    BetweennessEntry entry = new BetweennessEntry("Paper", 100.0);

    // For directed graph with 100 nodes: max = (99 * 98) = 9702
    double normalized = entry.getNormalizedBetweenness(100);

    assertEquals(100.0 / 9702.0, normalized, 0.0001);
  }

  @Test
  void testNormalizedBetweennessThrowsOnSmallGraph() {
    BetweennessEntry entry = new BetweennessEntry("Paper", 100.0);

    assertThrows(IllegalArgumentException.class, () -> entry.getNormalizedBetweenness(2));
  }

  @Test
  void testIsBridge() {
    BetweennessEntry highBetweenness = new BetweennessEntry("Hub", 5000.0);
    BetweennessEntry lowBetweenness = new BetweennessEntry("Leaf", 10.0);

    assertTrue(highBetweenness.isBridge(1000.0));
    assertFalse(lowBetweenness.isBridge(1000.0));
  }

  @Test
  void testCompareTo() {
    BetweennessEntry high = new BetweennessEntry("High", 1000.0);
    BetweennessEntry low = new BetweennessEntry("Low", 100.0);
    BetweennessEntry equal = new BetweennessEntry("Equal", 1000.0);

    assertTrue(high.compareTo(low) < 0); // high comes first (descending)
    assertTrue(low.compareTo(high) > 0);
    assertEquals(0, high.compareTo(equal));
  }

  @Test
  void testEqualsAndHashCode() {
    BetweennessEntry entry1 = new BetweennessEntry("P001", "Paper A", 500.0);
    BetweennessEntry entry2 = new BetweennessEntry("P001", "Paper A", 500.0);
    BetweennessEntry entry3 = new BetweennessEntry("P002", "Paper B", 600.0);

    assertEquals(entry1, entry2);
    assertNotEquals(entry1, entry3);
    assertEquals(entry1.hashCode(), entry2.hashCode());
  }

  @Test
  void testToString() {
    BetweennessEntry entry = new BetweennessEntry("P001", "Test", 123.4567);

    String str = entry.toString();

    assertTrue(str.contains("P001"));
    assertTrue(str.contains("Test"));
    assertTrue(str.contains("123.4567"));
  }

  @Test
  void testEquals_EdgeCases() {
    BetweennessEntry entry = new BetweennessEntry("P1", "Title", 10.0);

    assertFalse(entry.equals(null));
    assertFalse(entry.equals("Some String"));
    assertTrue(entry.equals(entry));
  }

  @Test
  void testEquals_PartialDifferences() {
    BetweennessEntry base = new BetweennessEntry("P1", "Title", 10.0);

    BetweennessEntry diffScore = new BetweennessEntry("P1", "Title", 20.0);
    assertNotEquals(base, diffScore);

    BetweennessEntry diffId = new BetweennessEntry("P2", "Title", 10.0);
    assertNotEquals(base, diffId);

    BetweennessEntry diffTitle = new BetweennessEntry("P1", "Other Title", 10.0);
    assertNotEquals(base, diffTitle);
  }

  @Test
  void testToJson_Failure() {
    // Create an anonymous subclass that throws exception on getter access
    // This forces ObjectMapper to catch the exception inside toJson
    BetweennessEntry faultyEntry =
        new BetweennessEntry() {
          @Override
          public String getTitle() {
            throw new RuntimeException("Force serialization error");
          }
        };

    RuntimeException ex = assertThrows(RuntimeException.class, faultyEntry::toJson);
    assertTrue(ex.getMessage().contains("Failed to serialize BetweennessEntry to JSON"));
  }
}
