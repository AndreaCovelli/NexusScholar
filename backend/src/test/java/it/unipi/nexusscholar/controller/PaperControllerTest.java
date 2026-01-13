package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.PaperService;
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

@WebMvcTest(PaperController.class)
class PaperControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private PaperService paperService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser(roles = "USER")
  void savePaper_ReturnsOk() throws Exception {
    PaperDTO inputDTO = new PaperDTO();
    inputDTO.setTitle("Test Paper");
    inputDTO.setYear(2023);

    PaperDTO savedDTO = new PaperDTO();
    savedDTO.setId("paper-1");
    savedDTO.setTitle("Test Paper");

    when(paperService.savePaper(any(PaperDTO.class))).thenReturn(savedDTO);

    mockMvc
        .perform(
            post("/api/papers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("paper-1"));
  }

  @Test
  @WithMockUser
  void getPaperById_ReturnsOk() throws Exception {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setId("paper-1");
    paperDTO.setTitle("Test Paper");

    when(paperService.getPaperById("paper-1")).thenReturn(paperDTO);

    mockMvc
        .perform(get("/api/papers/paper-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Test Paper"));
  }

  @Test
  @WithMockUser
  void searchPapersByTitle_ReturnsOk() throws Exception {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setTitle("Deep Learning");

    Page<PaperDTO> page = new PageImpl<>(List.of(paperDTO));

    when(paperService.searchPapersByTitle(eq("Deep"), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/papers/search").param("title", "Deep").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("Deep Learning"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithMockUser(roles = "USER")
  void smartSearch_ReturnsOk() throws Exception {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setTitle("Smart AI");

    Page<PaperDTO> page = new PageImpl<>(List.of(paperDTO));

    when(paperService.searchPapersByText(eq("AI"), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/papers/smart-search")
                .param("keyword", "AI")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("Smart AI"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void smartSearch_EmptyResults_ReturnsNoContent() throws Exception {
    when(paperService.searchPapersByText(eq("Unknown"), any(Pageable.class)))
        .thenReturn(Page.empty());

    mockMvc
        .perform(get("/api/papers/smart-search").param("keyword", "Unknown"))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser
  void getPapersByYear_ReturnsOk() throws Exception {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setTitle("Paper 2023");
    paperDTO.setYear(2023);

    Page<PaperDTO> page = new PageImpl<>(List.of(paperDTO));

    when(paperService.getPapersByYear(eq(2023), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/papers/year/2023"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].year").value(2023));
  }

  @Test
  @WithMockUser
  void getPapersByYear_EmptyResults_ReturnsNoContent() throws Exception {
    when(paperService.getPapersByYear(eq(1900), any(Pageable.class))).thenReturn(Page.empty());

    mockMvc.perform(get("/api/papers/year/1900")).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deletePaper_ReturnsNoContent() throws Exception {
    doNothing().when(paperService).deletePaper("paper-1");

    mockMvc.perform(delete("/api/papers/paper-1").with(csrf())).andExpect(status().isNoContent());

    verify(paperService).deletePaper("paper-1");
  }
}
