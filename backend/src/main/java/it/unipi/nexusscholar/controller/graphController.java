package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.service.GraphService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph/analysis")
@RequiredArgsConstructor
public class graphController {

  private final GraphService graphService;

  // Request for Pagerank computations
  @GetMapping("/pagerank")
  public ResponseEntity<List<PageRankDTO>> callPageRank() {
    List<PageRankDTO> res = graphService.pagerank();
    if (res.isEmpty()) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } else return ResponseEntity.ok(res);
  }

  @GetMapping("/shortestPath/{a1}&{a2}")
  public ResponseEntity<ShortestPathDTO> callShortestPath(
      @PathVariable String a1, @PathVariable String a2) {
    ShortestPathDTO res = graphService.collabPath(a1, a2);
    if (res == null) {
      return ResponseEntity.notFound().build();
    } else return ResponseEntity.ok(res);
  }

  @GetMapping("/leidenCommunities")
  public ResponseEntity<List<LeidenDTO>> callLeiden() {
    List<LeidenDTO> res = graphService.hiddenCommunities();
    if (res.isEmpty()) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } else return ResponseEntity.ok(res);
  }

  @GetMapping("/betweenness")
  public ResponseEntity<List<BetweennessDTO>> callBetweenness() {
    List<BetweennessDTO> res = graphService.betweenness();
    if (res.isEmpty()) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } else return ResponseEntity.ok(res);
  }
}
