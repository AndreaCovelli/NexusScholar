package it.unipi.nexusscholar.model.mongo;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "papers")
public class Paper {
    @Id
    private String id;

    private String title;
    private Integer year;
    @Field("dblp_key")
    private String dblpKey;
    private String doi;
    @Field("abstact")
    private String abstractText;
    @Field("fields_of_study")
    private List<String> fieldsOfStudy;
    private List<String> authors;
    private List<String> venue;
}
