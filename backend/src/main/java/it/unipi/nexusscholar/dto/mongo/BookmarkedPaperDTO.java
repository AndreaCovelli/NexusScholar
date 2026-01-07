package it.unipi.nexusscholar.dto.mongo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookmarkedPaperDTO {
  private String paperId;
  private String title;
  private LocalDateTime savedAt;
}
