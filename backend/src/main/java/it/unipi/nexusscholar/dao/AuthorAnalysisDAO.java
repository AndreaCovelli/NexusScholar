package it.unipi.nexusscholar.dao;

import java.util.List;

import it.unipi.nexusscholar.model.mongo.ProlificAuthor;

public interface AuthorAnalysisDAO {

    /**
     * Prolific Author Identification: Find authors who have published
     * more than “X” papers.
     * db.authors.aggregate([
     *     {
     *         $unwind: "$publications_summary"
     *     },
     *     {
     *         $group:
     *         {
     *             _id: {_id: "$_id", name: "$name", year: "$year"},
     *             paper_created: {$sum:1}
     *         }
     *     },
     *     {
     *         $match:
     *         {
     *             paper_created: {$gt: 5}
     *         }
     *     },
     *     {
     *         $group:
     *         {
     *             _id: "$_id._id",
     *             name: {$first: "$_id.name"}
     *         }
     *     }
     * ])
     * @param minPublications Number of minimum publications
     * @return List of the prolific authors
     */
  List<ProlificAuthor> getProlificAuthors(int minPublications);
}
