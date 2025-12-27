package it.unipi.nexusscholar.dao.neo4j;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DaoTest {

  @Test
  void testDaoInstantiation() {
    assertNotNull(new AuthorNodeDao());
    assertNotNull(new PaperNodeDao());
    assertNotNull(new TopicNodeDao());
  }
}
