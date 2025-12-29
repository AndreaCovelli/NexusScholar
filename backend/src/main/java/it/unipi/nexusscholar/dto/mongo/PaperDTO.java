package it.unipi.nexusscholar.dto.mongo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaperDTO {
    private String id;
    private String title;
    private Integer year;
    private String dblpKey;
    private String doi;
    private String abstractText;
    private List<String> fieldsOfStudy;
    private List<String> authors;
    private List<String> venue;
}
