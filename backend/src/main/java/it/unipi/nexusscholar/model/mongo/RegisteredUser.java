package it.unipi.nexusscholar.model.mongo;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "registeredUsers")
public class RegisteredUser extends User {

  @Field("full_name")
  private String fullName;

  @Field("bookmarked_papers")
  private List<BookmarkedPaper> bookmarkedPapers;
}
