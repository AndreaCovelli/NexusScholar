package it.unipi.nexusscholar.dto.mongo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedPaperDTO {
  private String paperId;
  private String title;
  private LocalDateTime savedAt;
}
