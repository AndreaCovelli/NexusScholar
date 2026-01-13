package it.unipi.nexusscholar.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import jakarta.servlet.http.HttpServletRequest;
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
  @Mock private HttpServletRequest request;

  @InjectMocks private JwtTokenProvider tokenProvider;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(
        tokenProvider, "secretKey", "super-secret-key-for-testing-purposes-only-12345");
    ReflectionTestUtils.setField(tokenProvider, "validityInMilliseconds", 3600000L);
    tokenProvider.init();
  }

  @Test
  void createAndValidateToken_User_Success() {
    String token = tokenProvider.createToken("user1", "u123", "USER");
    assertNotNull(token);

    when(userDAO.existsByUsername("user1")).thenReturn(true);
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void createAndValidateToken_User_NotFound() {
    String token = tokenProvider.createToken("user1", "u123", "USER");
    when(userDAO.existsByUsername("user1")).thenReturn(false);
    assertFalse(tokenProvider.validateToken(token));
  }

  @Test
  void createAndValidateToken_Admin_Success() {
    String token = tokenProvider.createToken("admin1", "a123", "ADMIN");

    when(adminDAO.existsByUsername("admin1")).thenReturn(true);
    assertTrue(tokenProvider.validateToken(token));
  }

  @Test
  void createAndValidateToken_Admin_NotFound() {
    String token = tokenProvider.createToken("admin1", "a123", "ADMIN");
    when(adminDAO.existsByUsername("admin1")).thenReturn(false);
    assertFalse(tokenProvider.validateToken(token));
  }

  @Test
  void validateToken_InvalidSignature() {
    String token = tokenProvider.createToken("user1", "u123", "USER");
    String tampered = token + "fail";
    assertFalse(tokenProvider.validateToken(tampered));
  }

  @Test
  void validateToken_NullToken() {
    assertFalse(tokenProvider.validateToken(null));
  }

  @Test
  void getClaimsFromToken() {
    String token = tokenProvider.createToken("user1", "u123", "USER");

    assertEquals("user1", tokenProvider.getUsernameFromToken(token));
    assertEquals("u123", tokenProvider.getUserIdFromToken(token));
  }

  @Test
  void resolveToken_BearerToken() {
    when(request.getHeader("Authorization")).thenReturn("Bearer abcdef");
    assertEquals("abcdef", tokenProvider.resolveToken(request));
  }

  @Test
  void resolveToken_NoBearer() {
    when(request.getHeader("Authorization")).thenReturn("Basic abcdef");
    assertNull(tokenProvider.resolveToken(request));
  }

  @Test
  void resolveToken_NullHeader() {
    when(request.getHeader("Authorization")).thenReturn(null);
    assertNull(tokenProvider.resolveToken(request));
  }

  @Test
  void getAuthentication_Success() {
    String token = tokenProvider.createToken("user1", "u123", "USER");
    var auth = tokenProvider.getAuthentication(token);
    assertNotNull(auth);
    assertEquals("user1", auth.getName());
    assertEquals(1, auth.getAuthorities().size());
    assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }
}
