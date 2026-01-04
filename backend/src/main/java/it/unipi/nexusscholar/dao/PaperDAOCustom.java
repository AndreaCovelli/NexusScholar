package it.unipi.nexusscholar.dao;

import java.util.List;

import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;
import org.bson.Document;

public interface PaperDAOCustom {

  /**
   * Calculate the number of papers published per Topic per year, segmented by source (DBLP vs
   * Semantic Scholar). This identifies emerging research trends.
   *
   * @return
   */
  List<TrendAnalysis> getTrendAnalysis();

  /**
   * Venue Impact Analysis: Ranking venues, published in a solar year, by number of papers created
   *
   * @return
   */
  List<VenueAnalysis> getVenueAnalysis();

  /**
   * Venue Impact Analysis: Ranking venues, published in a solar year, by number of papers created
   */
  List<CollaborationEvolution> getCollaborationEvolution();
}
