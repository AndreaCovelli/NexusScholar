package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorDTO {
    private String id;
    private String name;
    private String s2AuthorId;
    private Integer totalPublications;
    private List<PublicationSummaryDTO> publicationsSummary;

}