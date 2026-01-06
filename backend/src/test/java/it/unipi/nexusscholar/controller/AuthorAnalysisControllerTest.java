package it.unipi.nexusscholar.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.unipi.nexusscholar.dto.mongo.ProlificAuthorDTO;
import it.unipi.nexusscholar.service.AuthorAnalysisService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthorAnalysisController.class)
class AuthorAnalysisControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthorAnalysisService authorAnalysisService;

  @Test
  void getProlificAuthors_ReturnsOk() throws Exception {
    ProlificAuthorDTO author = new ProlificAuthorDTO("A001", "John Doe");

    when(authorAnalysisService.getProlificAuthors(10)).thenReturn(List.of(author));

    mockMvc
        .perform(get("/api/authors/analysis/prolific").param("minPublications", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].authorId").value("A001"));
  }

  @Test
  void getProlificAuthors_DefaultParameter() throws Exception {
    when(authorAnalysisService.getProlificAuthors(10)).thenReturn(List.of());

    mockMvc.perform(get("/api/authors/analysis/prolific")).andExpect(status().isNoContent());
  }

  @Test
  void getProlificAuthors_EmptyResults_ReturnsNoContent() throws Exception {
    when(authorAnalysisService.getProlificAuthors(100)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/authors/analysis/prolific").param("minPublications", "100"))
        .andExpect(status().isNoContent());
  }
}
