package it.unipi.nexusscholar.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.service.GraphService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GraphController.class)
class GraphControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private GraphService graphService;

  @Test
  void callPageRank_ReturnsOk() throws Exception {
    PageRankDTO pageRank = new PageRankDTO("Important Paper", 0.95);
    List<PageRankDTO> pageRanks = List.of(pageRank);
    when(graphService.pagerank(PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(pageRanks, PageRequest.of(0, 20), pageRanks.size()));

    mockMvc
        .perform(get("/api/graph/analysis/pagerank"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].paperTitle").value("Important Paper"))
        .andExpect(jsonPath("$[0].rank").value(0.95));
  }

  @Test
  void callShortestPath_ReturnsOk() throws Exception {
    ShortestPathDTO path = new ShortestPathDTO();
    path.setDegreeSeparation(2);
    path.setNodes(Collections.emptyList());
    path.setRelationships(Collections.emptyList());

    when(graphService.collabPath("Author1", "Author2")).thenReturn(path);

    mockMvc
        .perform(
            get("/api/graph/analysis/shortestPath").param("a1", "Author1").param("a2", "Author2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.degreeSeparation").value(2));
  }

  @Test
  void callShortestPath_NotFound_ReturnsNotFound() throws Exception {
    when(graphService.collabPath("Author1", "Author2")).thenReturn(null);

    mockMvc
        .perform(
            get("/api/graph/analysis/shortestPath").param("a1", "Author1").param("a2", "Author2"))
        .andExpect(status().isNotFound());
  }

  @Test
  void callLeiden_ReturnsOk() throws Exception {
    LeidenDTO community = new LeidenDTO(1, List.of("Author1", "Author2"));
    List<LeidenDTO> communityLeiden = List.of(community);

    when(graphService.hiddenCommunities(PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(communityLeiden, PageRequest.of(0, 20), communityLeiden.size()));

    mockMvc
        .perform(get("/api/graph/analysis/leidenCommunities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].communityId").value(1))
        .andExpect(jsonPath("$[0].authors[0]").value("Author1"));
  }

  @Test
  void callBetweenness_ReturnsOk() throws Exception {
    BetweennessDTO betweenness = new BetweennessDTO("Hub Paper", 1523.45);
    List<BetweennessDTO> betweennesses = List.of(betweenness);
    when(graphService.betweenness(PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(betweennesses, PageRequest.of(0, 20), betweennesses.size()));

    mockMvc
        .perform(get("/api/graph/analysis/betweenness"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].paperTitle").value("Hub Paper"))
        .andExpect(jsonPath("$[0].betweenness").value(1523.45));
  }
}
