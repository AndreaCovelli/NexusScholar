package it.unipi.nexusscholar.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.unipi.nexusscholar.dto.mongo.CollaborationEvolutionDTO;
import it.unipi.nexusscholar.dto.mongo.TrendAnalysisDTO;
import it.unipi.nexusscholar.dto.mongo.VenueAnalysisDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.PaperAnalysisService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaperAnalysisController.class)
class PaperAnalysisControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private PaperAnalysisService paperAnalysisService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser(roles = "USER")
  void getTrendAnalysis_ReturnsOk() throws Exception {
    TrendAnalysisDTO trend = new TrendAnalysisDTO("Machine Learning", 2023, 150);

    when(paperAnalysisService.getTrendAnalysis()).thenReturn(List.of(trend));

    mockMvc
        .perform(get("/api/papers/analysis/trend"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].fieldOfStudy").value("Machine Learning"))
        .andExpect(jsonPath("$[0].year").value(2023))
        .andExpect(jsonPath("$[0].paperCreated").value(150));
  }

  @Test
  @WithMockUser(roles = "USER")
  void getVenueAnalysis_ReturnsOk() throws Exception {
    VenueAnalysisDTO venue = new VenueAnalysisDTO("NeurIPS", 2023, 500);

    when(paperAnalysisService.getVenueAnalysis()).thenReturn(List.of(venue));

    mockMvc
        .perform(get("/api/papers/analysis/venue"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].venue").value("NeurIPS"))
        .andExpect(jsonPath("$[0].year").value(2023));
  }

  @Test
  @WithMockUser(roles = "USER")
  void getCollaborationEvolution_ReturnsOk() throws Exception {
    CollaborationEvolutionDTO collab = new CollaborationEvolutionDTO(3.5, 2023);

    when(paperAnalysisService.getCollaborationEvolution()).thenReturn(List.of(collab));

    mockMvc
        .perform(get("/api/papers/analysis/collaboration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].avgAuthors").value(3.5))
        .andExpect(jsonPath("$[0].year").value(2023));
  }
}
