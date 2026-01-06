package it.unipi.nexusscholar.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "papers")
public class Paper {
  @Id private String id;

  private String title;
  private Integer year;

  @Field("dblp_key")
  private String dblpKey;

  private String doi;

  @Field("abstract")
  private String abstractText;

  @Field("fields_of_study")
  private List<String> fieldsOfStudy;

  // CRITICAL CHANGE: Use embedded PaperAuthor objects instead of plain strings
  // This matches the data pipeline output from 3_integration.py
  private List<PaperAuthor> authors;

  private List<String> venue;
}
