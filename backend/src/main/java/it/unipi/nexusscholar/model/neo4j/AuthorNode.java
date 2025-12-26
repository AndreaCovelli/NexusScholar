//This class is used to model the instance of Author as a node in neo4j graphDb
//with properties name and authorId

package it.unipi.nexusscholar.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.List;

@org.springframework.data.neo4j.core.schema.Node
public class AuthorNode {

    //attributes
    @Id
    private String authorId;

    private String name;

    @Relationship(type = "AUTHORED", direction = Relationship.Direction.OUTGOING)
    private List<PaperNode> authPapers;

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PaperNode> getAuthPapers() {
        return authPapers;
    }

    public void setAuthPapers(List<PaperNode> authPapers) {
        this.authPapers = authPapers;
    }
}
