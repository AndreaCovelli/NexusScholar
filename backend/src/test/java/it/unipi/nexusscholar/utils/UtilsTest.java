package it.unipi.nexusscholar.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Path;

class UtilsTest {

  @Test
  void testPageRankEntry() {

    PageRankEntry entry = new PageRankEntry(0.85, "Graph Theory");
    assertEquals(0.85, entry.getRank());
    assertEquals("Graph Theory", entry.getPaperTitle());

    entry.setRank(0.99);
    entry.setPaperTitle("New Title");
    assertEquals(0.99, entry.getRank());
    assertEquals("New Title", entry.getPaperTitle());

    String json = entry.toJson();
    assertTrue(json.contains("\"paperTitle\":\"New Title\""));
    assertTrue(json.contains("\"rank\":0.99"));

    Record mockRecord = mock(Record.class);
    Value mockRank = mock(Value.class);
    Value mockTitle = mock(Value.class);

    when(mockRecord.get("rank")).thenReturn(mockRank);
    when(mockRecord.get("title")).thenReturn(mockTitle);
    when(mockRank.asDouble()).thenReturn(0.55);
    when(mockTitle.asString()).thenReturn("Record Title");

    PageRankEntry fromRecord = new PageRankEntry(mockRecord);
    assertEquals(0.55, fromRecord.getRank());
    assertEquals("Record Title", fromRecord.getPaperTitle());
  }

  @Test
  void testShortestPathAuthors() {
    Path mockPath = mock(Path.class);
    when(mockPath.toString()).thenReturn("path-string");

    ShortestPathAuthors spa = new ShortestPathAuthors(mockPath, 2);
    assertEquals(mockPath, spa.getShortestPath());
    assertEquals(2, spa.getDegreeSeparation());

    Path newMockPath = mock(Path.class);
    spa.setShortestPath(newMockPath);
    spa.setDegreeSeparation(3);
    assertEquals(newMockPath, spa.getShortestPath());
    assertEquals(3, spa.getDegreeSeparation());

    Record mockRecord = mock(Record.class);
    Value mockPathVal = mock(Value.class);
    Value mockDegreeVal = mock(Value.class);

    when(mockRecord.get("path")).thenReturn(mockPathVal);
    when(mockRecord.get("DegreeSeparation")).thenReturn(mockDegreeVal);
    when(mockPathVal.asPath()).thenReturn(mockPath);
    when(mockDegreeVal.asInt()).thenReturn(5);

    ShortestPathAuthors fromRecord = new ShortestPathAuthors(mockRecord);
    assertEquals(5, fromRecord.getDegreeSeparation());
    assertEquals(mockPath, fromRecord.getShortestPath());
  }

  @Test
  void testNSConstants()
      throws NoSuchMethodException,
          IllegalAccessException,
          InvocationTargetException,
          InstantiationException {

    assertEquals("nexusscholar", NSConstants.NEO4J_DB);
    assertEquals(7687, NSConstants.NEO4J_PORT);
    assertEquals("localhost", NSConstants.NEO4J_HOST);

    Constructor<NSConstants> constructor = NSConstants.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  void testLeidenCommunity() {
    LeidenCommunity lc = new LeidenCommunity(3);
    assertEquals(3, lc.getCommunityId());

    ArrayList<String> ls = new ArrayList<>();
    ls.add("Daniele");
    ls.add("Andrea");
    LeidenCommunity lc1 = new LeidenCommunity(2, ls);
    assertEquals(2, lc1.getCommunityId());
    assertNotNull(lc1.getAuthors());

    lc1.addAuthor("Luca");
    assertEquals("Luca", lc1.getAuthors().getLast());

    String json_lc1 = lc1.toJson();
    assertTrue(json_lc1.contains("\"communityId\":2"));
    assertTrue(json_lc1.contains("\"authors\":[\"Daniele\",\"Andrea\",\"Luca\"]"));
  }
}
