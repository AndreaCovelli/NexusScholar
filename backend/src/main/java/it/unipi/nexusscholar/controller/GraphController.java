package it.unipi.nexusscholar.controller;

import it.unipi.nexusscholar.dto.neo4j.BetweennessDTO;
import it.unipi.nexusscholar.dto.neo4j.LeidenDTO;
import it.unipi.nexusscholar.dto.neo4j.PageRankDTO;
import it.unipi.nexusscholar.dto.neo4j.ShortestPathDTO;
import it.unipi.nexusscholar.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph/analysis")
@RequiredArgsConstructor
public class GraphController {

  private final GraphService graphService;

  // Request for Pagerank computations
  @GetMapping("/pagerank")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<PageRankDTO>> callPageRank(
      @PageableDefault(size = 20, sort = "rank", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(graphService.pagerank(pageable));
  }

  @GetMapping("/shortestPath")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<ShortestPathDTO> callShortestPath(
      @RequestParam String a1, @RequestParam String a2) {
    ShortestPathDTO res = graphService.collabPath(a1, a2);
    if (res == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(res);
  }

  @GetMapping("/leidenCommunities")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<LeidenDTO>> callLeiden(@PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(graphService.hiddenCommunities(pageable));
  }

  @GetMapping("/betweenness")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<BetweennessDTO>> callBetweenness(
      @PageableDefault(size = 20, sort = "betweenness", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(graphService.betweenness(pageable));
  }
}
