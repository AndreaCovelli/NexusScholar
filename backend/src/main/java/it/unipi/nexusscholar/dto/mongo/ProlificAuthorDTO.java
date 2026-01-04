package it.unipi.nexusscholar.dto.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.web.bind.annotation.Mapping;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProlificAuthorDTO {
    private String authorId;
    private String authorName;
}
