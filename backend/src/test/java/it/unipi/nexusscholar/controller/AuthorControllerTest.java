package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.AuthorDTO;
import it.unipi.nexusscholar.service.AuthorService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private AuthorService authorService;

  @Test
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("author-1"))
        .andExpect(jsonPath("$.name").value("John Doe"));
  }

  @Test
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
  void searchAuthorsByName_ReturnsOk() throws Exception {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setName("John Doe");

    when(authorService.searchAuthorsByName("John")).thenReturn(List.of(authorDTO));

    mockMvc
        .perform(get("/api/authors/search").param("name", "John"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("John Doe"));
  }

  @Test
  void getAuthorsWithMinPublications_ReturnsOk() throws Exception {
    AuthorDTO authorDTO = new AuthorDTO();
    authorDTO.setName("Prolific Author");
    authorDTO.setTotalPublications(15);

    when(authorService.getAuthorsWithMinPublications(10)).thenReturn(List.of(authorDTO));

    mockMvc
        .perform(get("/api/authors/filter").param("min", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Prolific Author"));
  }

  @Test
  void deleteAuthorByS2Id_ReturnsOk() throws Exception {
    doNothing().when(authorService).deleteAuthorByS2Id("s2-123");

    mockMvc.perform(delete("/api/authors/s2/s2-123")).andExpect(status().isOk());

    verify(authorService).deleteAuthorByS2Id("s2-123");
  }
}
