package it.unipi.nexusscholar.dto.mongo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredUserUpdateDTO {
  private String email;
  private String password;
  private String fullName;
  private List<BookmarkedPaperDTO> bookmarkedPapers;
}
