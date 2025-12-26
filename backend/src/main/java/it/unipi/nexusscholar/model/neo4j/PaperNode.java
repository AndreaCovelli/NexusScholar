// This class is used to model the instance of Topic as a node in neo4j graphDb
// with properties name and paperId

package it.unipi.nexusscholar.model.neo4j;

import java.util.List;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@org.springframework.data.neo4j.core.schema.Node
public class PaperNode {

    //attributes

    // ID of papers is structured as PXXXXXX
    // where sequence of X are numbers
    @Id
    private String paperID;

    private String title;

    //relationship of the cites between papers
    @Relationship(type = "CITES", direction = Relationship.Direction.OUTGOING)
    private List<PaperNode> citedPapers;

    //relationship between paper and its topics
    @Relationship(type = "HAS_TOPIC", direction = Relationship.Direction.OUTGOING)
    private List<TopicNode> topics;

    @Relationship(type = "AUTHORED", direction = Relationship.Direction.INCOMING)
    private List<AuthorNode> authors;

    public List<AuthorNode> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorNode> authors) {
        this.authors = authors;
    }

    public List<TopicNode> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicNode> topics) {
        this.topics = topics;
    }

    public String getPaperID() {
        return paperID;
    }

    public void setPaperID(String paperID) {
        paperID = paperID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PaperNode> getCitedPapers() {
        return citedPapers;
    }

    public void setCitedPapers(List<PaperNode> citedPapers) {
        this.citedPapers = citedPapers;
    }
}
