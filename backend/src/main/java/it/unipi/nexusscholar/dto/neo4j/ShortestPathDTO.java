package it.unipi.nexusscholar.dto.neo4j;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.unipi.nexusscholar.utils.ShortestPathSerializer;
import org.neo4j.driver.types.Path;

import java.util.List;

public class ShortestPathDTO {

    private String nodes;
    private String edges;
    private int degreeSeparation;

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public String getEdges() {
        return edges;
    }

    public void setEdges(String edges) {
        this.edges = edges;
    }

    public int getDegreeSeparation() {
        return degreeSeparation;
    }

    public void setDegreeSeparation(int degreeSeparation) {
        this.degreeSeparation = degreeSeparation;
    }
}
