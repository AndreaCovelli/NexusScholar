package it.unipi.nexusscholar.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegisteredUser extends User {

    @Field("full_name")
    private String fullName;

    private String affiliation;

    @Builder.Default
    @Field("bookmarked_papers")
    private List<BookmarkedPaper> bookmarkedPapers = new ArrayList<>();
}