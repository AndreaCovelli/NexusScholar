package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.AuthorService;
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

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AuthorService authorService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser(roles = "USER")
  void saveAuthor_ReturnsOk() throws Exception {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setName("John Doe");
    authorDTO.setS2AuthorId("s2-123");

    AuthorDTO savedDTO = new AuthorDTO();
    savedDTO.setId("author-1");
    savedDTO.setName("John Doe");
    savedDTO.setS2AuthorId("s2-123");

    when(authorService.saveAuthor(any(AuthorDTO.class))).thenReturn(savedDTO);

    mockMvc
        .perform(
            post("/api/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("author-1"))
        .andExpect(jsonPath("$.name").value("John Doe"));
  }

  @Test
  @WithMockUser // Add this annotation
  void getAuthorByS2Id_ReturnsOk() throws Exception {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setId("author-1");
    authorDTO.setName("John Doe");
    authorDTO.setS2AuthorId("s2-123");

    when(authorService.getAuthorByS2Id("s2-123")).thenReturn(authorDTO);

    mockMvc
        .perform(get("/api/authors/s2/s2-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"));
  }

  @Test
  @WithMockUser // Add this annotation
  void searchAuthorsByName_ReturnsOk() throws Exception {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setName("John Doe");

    Page<AuthorDTO> page = new PageImpl<>(List.of(authorDTO));

    when(authorService.searchAuthorsByName(eq("John"), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/authors/search").param("name", "John").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("John Doe"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithMockUser // Add this annotation
  void getAuthorsWithMinPublications_ReturnsOk() throws Exception {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setName("Prolific Author");
    authorDTO.setTotalPublications(15);

    Page<AuthorDTO> page = new PageImpl<>(List.of(authorDTO));

    when(authorService.getAuthorsWithMinPublications(eq(10), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/authors/filter").param("min", "10").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Prolific Author"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteAuthorById_ReturnsOk() throws Exception {
    doNothing().when(authorService).deleteAuthorById("author-1");

    mockMvc.perform(delete("/api/authors/author-1").with(csrf())).andExpect(status().isOk());

    verify(authorService).deleteAuthorById("author-1");
  }
}
