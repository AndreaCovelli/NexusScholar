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
@Document(collection = "authors")
public class Author {
    @Id
    private String id;

    private String name;
    @Field("s2_author_id")
    private String s2AuthorId;
    @Field("total_publications")
    private Integer totalPublications;
    @Field("publication_summary")
    private List<PublicationSummary> publicationsSummary;

}
