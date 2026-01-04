package it.unipi.nexusscholar.dao;

import java.util.List;

import it.unipi.nexusscholar.model.mongo.CollaborationEvolution;
import it.unipi.nexusscholar.model.mongo.TrendAnalysis;
import it.unipi.nexusscholar.model.mongo.VenueAnalysis;

public interface PaperAnalysisDAO {

    /**
     * Trend Analysis (Hot Topics): Calculate the number of papers published per Topic per year.
     * This identifies emerging research trends.
     * db.papers.aggregate([
     *     {
     *         $unwind: "$fields_of_study"
     *     },
     *     {
     *         $group:
     *         {
     *             _id: {field_of_study:"$fields_of_study", year:"$year"},
     *             paper_created: {$sum:1}
     *         }
     *     },
     *     {
     *         $sort: {"_id.year":1}
     *     }
     * ])
     * @return List of the number of papers published per topic per year
     */
  List<TrendAnalysis> getTrendAnalysis();

  /**
   * Venue Impact Analysis: Ranking venues, published in a solar year, by number of papers created
   * db.papers.aggregate([
   *     {
   *         $unwind: "$venue"
   *     },
   *     {
   *         $group:
   *         {
   *             _id: { venue: "$venue", year: "$year"},
   *             paper_created: {$sum : 1}
   *         }
   *     },
   *     {
   *         $sort: {"_id.year":1, paper_created:-1}
   *     }
   * ])
   *
   * @return List of the ranked venues per year
   */
  List<VenueAnalysis> getVenueAnalysis();

    /**
     * Collaboration Evolution: Calculating the average number of authors per paper for each year
     * db.papers.aggregate([
     *     {
     *         $project:
     *         {
     *             year: "$year",
     *             num_authors: {$size: "$authors"}
     *         }
     *     },
     *     {
     *         $group:
     *         {
     *             _id:{year: "$year"},
     *             avg_authors:{$avg: "$num_authors"}
     *         }
     *     },
     *     {
     *         $sort: {"_id.year":1}
     *     }
     * ])
     * @return List of the average number of author per paper for each year
     */
  List<CollaborationEvolution> getCollaborationEvolution();
}
