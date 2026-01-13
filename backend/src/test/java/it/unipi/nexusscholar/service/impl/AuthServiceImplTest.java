package it.unipi.nexusscholar.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.mongo.AdminDAO;
import it.unipi.nexusscholar.dao.mongo.RegisteredUserDAO;
import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;
import it.unipi.nexusscholar.model.mongo.Admin;
import it.unipi.nexusscholar.model.mongo.RegisteredUser;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private AdminDAO adminDAO;
  @Mock private RegisteredUserDAO userDAO;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  void loginAdmin_Success() {
    Admin admin = new Admin();
    admin.setId("a1");
    admin.setUsername("admin");
    admin.setPassword("encoded");

    LoginRequestDTO req = new LoginRequestDTO("admin", "raw");

    when(adminDAO.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
    when(jwtTokenProvider.createToken("admin", "a1", "ADMIN")).thenReturn("token.abc");

    AuthResponseDTO response = authService.loginAdmin(req);
    assertEquals("token.abc", response.getAccessToken());
  }

  @Test
  void loginUser_BadCredentials() {
    RegisteredUser user = new RegisteredUser();
    user.setUsername("user");
    user.setPassword("encoded");

    LoginRequestDTO req = new LoginRequestDTO("user", "wrong");

    when(userDAO.findByUsername("user")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.loginRegisteredUser(req));
  }

  @Test
  void loginAdmin_UsernameNotFound_ThrowsBadCredentials() {
    LoginRequestDTO req = new LoginRequestDTO("unknown_admin", "password");

    when(adminDAO.findByUsername("unknown_admin")).thenReturn(Optional.empty());

    BadCredentialsException ex =
        assertThrows(BadCredentialsException.class, () -> authService.loginAdmin(req));

    assertTrue(ex.getMessage().contains("Admin not found"));
  }

  @Test
  void loginUser_UsernameNotFound_ThrowsBadCredentials() {
    LoginRequestDTO req = new LoginRequestDTO("unknown_user", "password");

    when(registeredUserDAO.findByUsername("unknown_user")).thenReturn(Optional.empty());

    BadCredentialsException ex =
        assertThrows(BadCredentialsException.class, () -> authService.loginRegisteredUser(req));

    assertTrue(ex.getMessage().contains("User not found"));
  }
}
