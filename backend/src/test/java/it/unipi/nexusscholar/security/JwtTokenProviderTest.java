package it.unipi.nexusscholar.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

  @Mock private RegisteredUserDAO userDAO;
  @Mock private AdminDAO adminDAO;

  @InjectMocks private JwtTokenProvider tokenProvider;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(
        tokenProvider, "secretKey", "super-secret-key-for-testing-purposes-only-12345");
    ReflectionTestUtils.setField(tokenProvider, "validityInMilliseconds", 3600000L);
    tokenProvider.init();
  }

  @Test
  void createAndValidateToken_User() {
    String token = tokenProvider.createToken("user1", "u123", "USER");
    assertNotNull(token);

    when(userDAO.existsByUsername("user1")).thenReturn(true);
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void createAndValidateToken_Admin() {
    String token = tokenProvider.createToken("admin1", "a123", "ADMIN");

    when(adminDAO.existsByUsername("admin1")).thenReturn(true);
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void validateToken_InvalidSignature() {
    String token = tokenProvider.createToken("user1", "u123", "USER");
    String tampered = token + "fail";
    assertFalse(tokenProvider.validateToken(tampered));
  }

  @Test
  void getClaimsFromToken() {
    String token = tokenProvider.createToken("user1", "u123", "USER");

    assertEquals("user1", tokenProvider.getUsernameFromToken(token));
    assertEquals("u123", tokenProvider.getUserIdFromToken(token));
  }
}
