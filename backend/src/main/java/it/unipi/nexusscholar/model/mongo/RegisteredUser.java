package it.unipi.nexusscholar.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "registeredUsers")
public class RegisteredUser extends User {

  @Field("full_name")
  private String fullName;

  @Field("bookmarked_papers")
  private List<BookmarkedPaper> bookmarkedPapers;
}
