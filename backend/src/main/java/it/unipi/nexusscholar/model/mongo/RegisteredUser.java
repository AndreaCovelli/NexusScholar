package it.unipi.nexusscholar.model.mongo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document representing a standard registered user.
 * <p>
 * This class extends the base {@link User} class and adds fields specific to
 * registered members, such as their full name and a personal collection of
 * bookmarked papers.
 * </p>
 */
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "registeredUsers")
public class RegisteredUser extends User {

  /**
   * The user's full legal name or display name.
   */
  @Field("full_name")
  private String fullName;

  /**
   * A list of papers saved/bookmarked by the user.
   */
  @Field("bookmarked_papers")
  private List<BookmarkedPaper> bookmarkedPapers;
}