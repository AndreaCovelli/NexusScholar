package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.BookmarkedPaperDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserCreateDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserDTO;
import it.unipi.nexusscholar.dto.mongo.RegisteredUserUpdateDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.RegisteredUserService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

  @Test
  @WithMockUser(roles = "USER")
  void updateUser_ReturnsOk() throws Exception {
    RegisteredUserDTO updated = new RegisteredUserDTO();
    updated.setId("u1");
    updated.setUsername("updatedUser");

    when(userService.updateUser(eq("u1"), any(RegisteredUserUpdateDTO.class))).thenReturn(updated);

    mockMvc
        .perform(
            put("/api/users/u1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisteredUserUpdateDTO())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updatedUser"));
  }

  @Test
  @WithMockUser
  void searchUsers_ReturnsList() throws Exception {
    when(userService.searchUsersByFullName("Mario")).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/users/search").param("name", "Mario"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAllUsers_ReturnsPage() throws Exception {
    Page<RegisteredUserDTO> page = new PageImpl<>(List.of(new RegisteredUserDTO()));
    when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteUser_ReturnsNoContent() throws Exception {
    doNothing().when(userService).deleteUser("u1");

    mockMvc.perform(delete("/api/users/u1").with(csrf())).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  void addBookmark_ReturnsOk() throws Exception {
    String token = "valid-token";
    when(jwtTokenProvider.resolveToken(any())).thenReturn(token);
    when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn("u1");

    RegisteredUserDTO dto = new RegisteredUserDTO();
    dto.setBookmarkedPapers(List.of(new BookmarkedPaperDTO("p1", "Title", null)));

    when(userService.addBookmark("u1", "p1")).thenReturn(dto);

    mockMvc
        .perform(
            post("/api/users/bookmarks/p1").header("Authorization", "Bearer " + token).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookmarkedPapers[0].paperId").value("p1"));
  }

  @Test
  @WithMockUser
  void getCurrentUserInfo_ReturnsOk() throws Exception {
    String token = "valid-token";
    when(jwtTokenProvider.resolveToken(any())).thenReturn(token);
    when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn("u1");
    when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("user1");

    mockMvc
        .perform(get("/api/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("u1"))
        .andExpect(jsonPath("$.username").value("user1"));
  }
}
