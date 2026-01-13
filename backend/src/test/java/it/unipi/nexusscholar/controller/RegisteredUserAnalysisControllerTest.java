package it.unipi.nexusscholar.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import it.unipi.nexusscholar.security.JwtTokenProvider;
import it.unipi.nexusscholar.service.RegisteredUserAnalysisService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegisteredUserAnalysisController.class)
class RegisteredUserAnalysisControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private RegisteredUserAnalysisService service;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Test
  @WithMockUser(roles = "USER")
  void getLeaderboard_Success_WithData() throws Exception {
    PaperLeaderboardDTO dto = new PaperLeaderboardDTO("p1", "Popular Paper", 100);
    when(service.getPaperLeaderboard(2025, 1)).thenReturn(List.of(dto));

    mockMvc
        .perform(get("/api/users/analysis/leaderboard").param("year", "2025").param("month", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].paperId").value("p1"))
        .andExpect(jsonPath("$[0].bookmarkedReceived").value(100));
  }

  @Test
  @WithMockUser(roles = "USER")
  void getLeaderboard_NoContent() throws Exception {
    when(service.getPaperLeaderboard(2025, 1)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/api/users/analysis/leaderboard").param("year", "2025").param("month", "1"))
        .andExpect(status().isNoContent());
  }
}
