package it.unipi.nexusscholar.service.impl;

import it.unipi.nexusscholar.dao.RegisteredUserAnalysisDAO;
import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;
import it.unipi.nexusscholar.model.mongo.PaperLeaderboard;
import it.unipi.nexusscholar.service.RegisteredUserAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisteredUserAnalysisServiceImpl implements RegisteredUserAnalysisService {

    private final RegisteredUserAnalysisDAO registeredUserAnalysisDAO;

    @Override
    public List<PaperLeaderboardDTO> getPaperLeaderboard(int year, int month) {
        List<PaperLeaderboard> leaderboard = registeredUserAnalysisDAO.getMostBookmarkedPapers(year, month);
        return leaderboard.stream().map(this::toPaperLeaderboardDTO).toList();
    }

    private PaperLeaderboardDTO toPaperLeaderboardDTO(PaperLeaderboard entity) {
        PaperLeaderboardDTO dto = new PaperLeaderboardDTO();
        dto.setPaperId(entity.getPaperId());
        dto.setTitle(entity.getTitle());
        dto.setBookmarkedReceived(entity.getBookmarkedReceived());
        return dto;
    }
}
