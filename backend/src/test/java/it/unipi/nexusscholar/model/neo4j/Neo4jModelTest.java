package it.unipi.nexusscholar.model.neo4j;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class Neo4jModelTest {

  @Test
  void testAuthorNode() {
    AuthorNode author = new AuthorNode();
    author.setAuthorId("A001");
    author.setName("John Doe");

    List<PaperNode> papers = new ArrayList<>();
    PaperNode paper = new PaperNode();
    paper.setPaperID("P001");
    papers.add(paper);
    author.setAuthPapers(papers);

    assertEquals("A001", author.getAuthorId());
    assertEquals("John Doe", author.getName());
    assertEquals(1, author.getAuthPapers().size());
    assertEquals("P001", author.getAuthPapers().get(0).getPaperID());
  }

  @Test
  void testTopicNode() {
    TopicNode topic = new TopicNode();
    topic.setTopicId("T001");
    topic.setName("Machine Learning");

    assertEquals("T001", topic.getTopicId());
    assertEquals("Machine Learning", topic.getName());
  }

  @Test
  void testPaperNode() {
    PaperNode paper = new PaperNode();
    paper.setPaperID("P001");
    paper.setTitle("Deep Learning");

    // Test Authors
    AuthorNode author = new AuthorNode();
    author.setAuthorId("A001");
    List<AuthorNode> authors = new ArrayList<>();
    authors.add(author);
    paper.setAuthors(authors);

    // Test Topics
    TopicNode topic = new TopicNode();
    topic.setTopicId("T001");
    List<TopicNode> topics = new ArrayList<>();
    topics.add(topic);
    paper.setTopics(topics);

    // Test Cited Papers
    PaperNode citedPaper = new PaperNode();
    citedPaper.setPaperID("P002");
    List<PaperNode> citedList = new ArrayList<>();
    citedList.add(citedPaper);
    paper.setCitedPapers(citedList);

    assertEquals("P001", paper.getPaperID());
    assertEquals("Deep Learning", paper.getTitle());
    assertEquals("A001", paper.getAuthors().get(0).getAuthorId());
    assertEquals("T001", paper.getTopics().get(0).getTopicId());
    assertEquals("P002", paper.getCitedPapers().get(0).getPaperID());
  }
}
