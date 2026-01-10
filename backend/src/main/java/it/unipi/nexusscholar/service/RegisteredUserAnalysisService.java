package it.unipi.nexusscholar.service;

import it.unipi.nexusscholar.dto.mongo.PaperLeaderboardDTO;

import java.util.List;

public interface RegisteredUserAnalysisService {
    List<PaperLeaderboardDTO> getPaperLeaderboard(int year, int month);
}
