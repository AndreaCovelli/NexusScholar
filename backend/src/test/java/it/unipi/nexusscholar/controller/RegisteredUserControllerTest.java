package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.RegisteredUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegisteredUserController.class)
class RegisteredUserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private RegisteredUserService userService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser
  void registerUser_ReturnsOk() throws Exception {
    RegisteredUserDTO created = new RegisteredUserDTO();
    created.setUsername("testuser");

    when(userService.registerUser(any(RegisteredUserCreateDTO.class))).thenReturn(created);

    mockMvc
        .perform(
            post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisteredUserCreateDTO())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testuser"));
  }

  @Test
  @WithMockUser
  void getUserById_ReturnsOk() throws Exception {
    RegisteredUserDTO user = new RegisteredUserDTO();
    user.setId("u1");
    when(userService.getUserById("u1")).thenReturn(user);

    mockMvc
        .perform(get("/api/users/u1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("u1"));
  }
}
