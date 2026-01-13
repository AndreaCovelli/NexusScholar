package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;

import it.unipi.nexusscholar.TestcontainersConfiguration;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RegisteredUserDAOIntegrationTest {

  @Autowired private RegisteredUserDAO userDAO;

  @BeforeEach
  void setUp() {
    userDAO.deleteAll();

    RegisteredUser u1 = new RegisteredUser();
    u1.setId("u1");
    u1.setUsername("user1");
    u1.setEmail("user1@test.com");
    u1.setFullName("Mario Rossi");

    RegisteredUser u2 = new RegisteredUser();
    u2.setId("u2");
    u2.setUsername("user2");
    u2.setEmail("user2@test.com");
    u2.setFullName("Luigi Verdi");

    userDAO.save(u1);
    userDAO.save(u2);
  }

  @Test
  void existsByEmailAndIdNot_SameId_ReturnsFalse() {
    // updating u1 with its own email should return false (valid)
    assertFalse(userDAO.existsByEmailAndIdNot("user1@test.com", "u1"));
  }

  @Test
  void existsByEmailAndIdNot_DifferentId_ReturnsTrue() {
    // updating u2 with u1's email should return true (invalid/conflict)
    assertTrue(userDAO.existsByEmailAndIdNot("user1@test.com", "u2"));
  }

  @Test
  void existsByUsernameAndIdNot_SameId_ReturnsFalse() {
    assertFalse(userDAO.existsByUsernameAndIdNot("user1", "u1"));
  }

  @Test
  void existsByUsernameAndIdNot_DifferentId_ReturnsTrue() {
    assertTrue(userDAO.existsByUsernameAndIdNot("user1", "u2"));
  }

  @Test
  void findByFullNameStartingWith_PrefixMatch() {
    var results = userDAO.findByFullNameStartingWith("Mario");
    assertEquals(1, results.size());
    assertEquals("Mario Rossi", results.get(0).getFullName());
  }
}
