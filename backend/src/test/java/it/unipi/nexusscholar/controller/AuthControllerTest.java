package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.auth.AuthResponseDTO;
import it.unipi.nexusscholar.dto.auth.LoginRequestDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AuthService authService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser
  void loginAdmin_Success() throws Exception {
    when(authService.loginAdmin(any(LoginRequestDTO.class)))
        .thenReturn(new AuthResponseDTO("admin-jwt"));

    LoginRequestDTO request = new LoginRequestDTO("admin", "password");

    mockMvc
        .perform(
            post("/api/auth/admin/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("admin-jwt"));
  }

  @Test
  @WithMockUser
  void loginUser_Success() throws Exception {
    when(authService.loginRegisteredUser(any(LoginRequestDTO.class)))
        .thenReturn(new AuthResponseDTO("user-jwt"));

    LoginRequestDTO request = new LoginRequestDTO("user", "password");

    mockMvc
        .perform(
            post("/api/auth/user/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("user-jwt"));
  }

  @Test
  @WithMockUser
  void loginUser_InvalidCredentials_ReturnsUnauthorized() throws Exception {
    when(authService.loginRegisteredUser(any()))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    mockMvc
        .perform(
            post("/api/auth/user/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"u\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }
}
