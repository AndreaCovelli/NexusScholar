package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.service.PaperService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaperController.class)
class PaperControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private PaperService paperService;

  @Test
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("paper-1"));
  }

  @Test
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
  void searchPapersByTitle_ReturnsOk() throws Exception {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setTitle("Deep Learning");

    when(paperService.searchPapersByTitle("Deep")).thenReturn(List.of(paperDTO));

    mockMvc
        .perform(get("/api/papers/search").param("title", "Deep"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Deep Learning"));
  }

  @Test
  void searchPapersByTitle_EmptyResults_ReturnsNoContent() throws Exception {
    when(paperService.searchPapersByTitle("NonExistent")).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/papers/search").param("title", "NonExistent"))
        .andExpect(status().isNoContent());
  }

  @Test
  void getPapersByYear_ReturnsOk() throws Exception {
    PaperDTO paperDTO = new PaperDTO();
    paperDTO.setTitle("Paper 2023");
    paperDTO.setYear(2023);

    when(paperService.getPapersByYear(2023)).thenReturn(List.of(paperDTO));

    mockMvc
        .perform(get("/api/papers/year/2023"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].year").value(2023));
  }

  @Test
  void getPapersByYear_EmptyResults_ReturnsNoContent() throws Exception {
    when(paperService.getPapersByYear(1900)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/papers/year/1900")).andExpect(status().isNoContent());
  }

  @Test
  void deletePaper_ReturnsNoContent() throws Exception {
    doNothing().when(paperService).deletePaper("paper-1");

    mockMvc.perform(delete("/api/papers/paper-1")).andExpect(status().isNoContent());

    verify(paperService).deletePaper("paper-1");
  }
}
