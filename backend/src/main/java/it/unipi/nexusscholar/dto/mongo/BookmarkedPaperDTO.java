package it.unipi.nexusscholar.dto.mongo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookmarkedPaperDTO {
    private String paperId;
    private String title;
    private LocalDateTime savedAt;
}