package it.unipi.nexusscholar.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Path;

class ShortestPathAuthorsTest {

  @Test
  void testConstructorWithValues() {
    Path mockPath = mock(Path.class);

    ShortestPathAuthors spa = new ShortestPathAuthors(mockPath, 3);

    assertEquals(mockPath, spa.getShortestPath());
    assertEquals(3, spa.getDegreeSeparation());
  }

  @Test
  void testConstructorWithRecord() {
    Record mockRecord = mock(Record.class);
    Value mockPathValue = mock(Value.class);
    Value mockDegreeValue = mock(Value.class);
    Path mockPath = mock(Path.class);

    when(mockRecord.get("path")).thenReturn(mockPathValue);
    when(mockRecord.get("DegreeSeparation")).thenReturn(mockDegreeValue);
    when(mockPathValue.asPath()).thenReturn(mockPath);
    when(mockDegreeValue.asInt()).thenReturn(2);

    ShortestPathAuthors spa = new ShortestPathAuthors(mockRecord);

    assertEquals(mockPath, spa.getShortestPath());
    assertEquals(2, spa.getDegreeSeparation());
  }

  @Test
  void testSetShortestPath() {
    Path originalPath = mock(Path.class);
    Path newPath = mock(Path.class);
    ShortestPathAuthors spa = new ShortestPathAuthors(originalPath, 1);

    spa.setShortestPath(newPath);

    assertEquals(newPath, spa.getShortestPath());
  }

  @Test
  void testSetDegreeSeparation() {
    Path mockPath = mock(Path.class);
    ShortestPathAuthors spa = new ShortestPathAuthors(mockPath, 1);

    spa.setDegreeSeparation(5);

    assertEquals(5, spa.getDegreeSeparation());
  }

  @Test
  void testWithZeroDegreeSeparation() {
    Path mockPath = mock(Path.class);

    ShortestPathAuthors spa = new ShortestPathAuthors(mockPath, 0);

    assertEquals(0, spa.getDegreeSeparation());
  }
}
