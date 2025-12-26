//This class is used to model the instance of Topic as a node in neo4j graphDb
//with properties name and topicId

package it.unipi.nexusscholar.model.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.List;

@org.springframework.data.neo4j.core.schema.Node
public class TopicNode {

    //attributes

    @Id
    private String  topicId;

    private String name;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
