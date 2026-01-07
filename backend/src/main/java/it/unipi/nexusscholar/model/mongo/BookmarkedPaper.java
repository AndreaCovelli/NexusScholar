package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkedPaper {

    @Field("paper_id")
    private String paperId;

    private String title;

    @Field("saved_at")
    private LocalDateTime savedAt;
}