package it.unipi.nexusscholar.dto.neo4j;

import org.neo4j.driver.types.Path;

public class ShortestPathDTO {

    private Path shortestPath;
    private int degreeSeparation;


    public Path getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(Path shortestPath) {
        this.shortestPath = shortestPath;
    }

    public int getDegreeSeparation() {
        return degreeSeparation;
    }

    public void setDegreeSeparation(int degreeSeparation) {
        this.degreeSeparation = degreeSeparation;
    }
}
