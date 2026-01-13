package it.unipi.nexusscholar.dto.mongo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Summary of a publication, typically used in lists.")
public class PublicationSummaryDTO {

  @Schema(description = "The unique identifier of the paper.", example = "609c1234567890abcdef1234")
  private String paperId;

  @Schema(description = "The year of publication.", example = "2023")
  private Integer year;

  @Schema(description = "The title of the paper.", example = "Advances in Neural Networks")
  private String title;
}