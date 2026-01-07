package it.unipi.nexusscholar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.nexusscholar.dto.mongo.PaperDTO;
import it.unipi.nexusscholar.service.PaperService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    // Wrap the result in a Page object
    Page<PaperDTO> page = new PageImpl<>(List.of(paperDTO));

    // Match the method signature: (String, Pageable)
    when(paperService.searchPapersByTitle(eq("Deep"), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(
            get("/api/papers/search").param("title", "Deep").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        // JSON structure changes: items are now inside "content"
        .andExpect(jsonPath("$.content[0].title").value("Deep Learning"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void searchPapersByTitle_EmptyResults_ReturnsNoContent() throws Exception {
    // Return an empty Page
    when(paperService.searchPapersByTitle(eq("NonExistent"), any(Pageable.class)))
        .thenReturn(Page.empty());

    mockMvc
        .perform(get("/api/papers/search").param("title", "NonExistent"))
        .andExpect(status().isNoContent());
  }

  @Test
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
  void getPapersByYear_EmptyResults_ReturnsNoContent() throws Exception {
    when(paperService.getPapersByYear(eq(1900), any(Pageable.class))).thenReturn(Page.empty());

    mockMvc.perform(get("/api/papers/year/1900")).andExpect(status().isNoContent());
  }

  @Test
  void deletePaper_ReturnsNoContent() throws Exception {
    doNothing().when(paperService).deletePaper("paper-1");

    mockMvc.perform(delete("/api/papers/paper-1")).andExpect(status().isNoContent());

    verify(paperService).deletePaper("paper-1");
  }
}
