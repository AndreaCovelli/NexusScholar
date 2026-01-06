package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.service.GraphService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph/analysis")
@RequiredArgsConstructor
public class GraphController {

  private final GraphService graphService;

  // Request for Pagerank computations
  @GetMapping("/pagerank")
  public ResponseEntity<List<PageRankDTO>> callPageRank() {
    return ResponseEntity.ok(graphService.pagerank());
  }

  @GetMapping("/shortestPath")
  public ResponseEntity<ShortestPathDTO> callShortestPath(
      @RequestParam String a1, @RequestParam String a2) {
    ShortestPathDTO res = graphService.collabPath(a1, a2);
    if (res == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(res);
  }

  @GetMapping("/leidenCommunities")
  public ResponseEntity<List<LeidenDTO>> callLeiden() {
    return ResponseEntity.ok(graphService.hiddenCommunities());
  }

  @GetMapping("/betweenness")
  public ResponseEntity<List<BetweennessDTO>> callBetweenness() {
    return ResponseEntity.ok(graphService.betweenness());
  }
}
