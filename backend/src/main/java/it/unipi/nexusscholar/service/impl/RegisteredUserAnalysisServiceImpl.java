package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.RegisteredUserAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import it.unipi.nexusscholar.service.RegisteredUserAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link RegisteredUserAnalysisService}.
 * <p>
 * This class coordinates the retrieval of raw aggregation results from the DAO
 * and transforms them into Data Transfer Objects (DTOs) suitable for the API response.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RegisteredUserAnalysisServiceImpl implements RegisteredUserAnalysisService {

  private final RegisteredUserAnalysisDAO registeredUserAnalysisDAO;

  /**
   * {@inheritDoc}
   * <p>
   * This implementation fetches the raw aggregation data from MongoDB and maps
   * each {@link PaperLeaderboard} entity to a {@link PaperLeaderboardDTO}.
   * </p>
   */
  @Override
  public List<PaperLeaderboardDTO> getPaperLeaderboard(int year, int month) {
    List<PaperLeaderboard> leaderboard =
            registeredUserAnalysisDAO.getMostBookmarkedPapers(year, month);
    return leaderboard.stream().map(this::toPaperLeaderboardDTO).toList();
  }

  /**
   * Mapper method to convert the internal entity to a public DTO.
   *
   * @param entity The database entity result.
   * @return The DTO to be returned to the client.
   */
  private PaperLeaderboardDTO toPaperLeaderboardDTO(PaperLeaderboard entity) {
    PaperLeaderboardDTO dto = new PaperLeaderboardDTO();
    dto.setPaperId(entity.getPaperId());
    dto.setTitle(entity.getTitle());
    dto.setBookmarkedReceived(entity.getBookmarkedReceived());
    return dto;
  }
}