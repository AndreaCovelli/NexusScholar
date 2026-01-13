package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuthorDAOIntegrationTest {

  @Autowired private AuthorDAO authorDAO;

  @BeforeEach
  void setUp() {
    authorDAO.deleteAll();

    Author a1 = new Author();
    a1.setId("a1");
    a1.setName("Alice Wonderland");
    a1.setS2AuthorId("S2-001");
    a1.setTotalPublications(15);

    Author a2 = new Author();
    a2.setId("a2");
    a2.setName("Bob Builder");
    a2.setS2AuthorId("S2-002");
    a2.setTotalPublications(5);

    authorDAO.save(a1);
    authorDAO.save(a2);
  }

  @Test
  void findByS2AuthorId_Found() {
    assertTrue(authorDAO.findByS2AuthorId("S2-001").isPresent());
  }

  @Test
  void findByNameStartsWith_Found() {
    Page<Author> page = authorDAO.findByNameStartsWith("Alice", PageRequest.of(0, 10));
    assertEquals(1, page.getTotalElements());
    assertEquals("Alice Wonderland", page.getContent().get(0).getName());
  }

  @Test
  void findByTotalPublicationsGreaterThan_FilterWorks() {
    Page<Author> page = authorDAO.findByTotalPublicationsGreaterThan(10, PageRequest.of(0, 10));
    assertEquals(1, page.getTotalElements());
    assertEquals("Alice Wonderland", page.getContent().get(0).getName());
  }
}
