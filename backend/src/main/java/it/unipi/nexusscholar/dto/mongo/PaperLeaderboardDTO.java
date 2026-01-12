package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperLeaderboardDTO {
  private String paperId;
  private String title;
  private int bookmarkedReceived;
}
