package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AdminDAOIntegrationTest {

  @Autowired private AdminDAO adminDAO;

  @BeforeEach
  void setUp() {
    adminDAO.deleteAll();
    Admin admin = new Admin();
    admin.setUsername("superadmin");
    admin.setEmail("admin@nexus.com");
    adminDAO.save(admin);
  }

  @Test
  void findByUsername_Found() {
    assertTrue(adminDAO.findByUsername("superadmin").isPresent());
  }

  @Test
  void findByUsernameStartingWith_Found() {
    var results = adminDAO.findByUsernameStartingWith("super");
    assertEquals(1, results.size());
  }

  @Test
  void existsByEmail_True() {
    assertTrue(adminDAO.existsByEmail("admin@nexus.com"));
  }
}
