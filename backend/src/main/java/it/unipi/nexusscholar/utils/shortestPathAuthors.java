package it.unipi.nexusscholar.utils;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;

import java.util.List;
import java.util.stream.Collectors;

public class shortestPathAuthors {

    private Path shortestPath;
    private int DegreeSeparation;

    public shortestPathAuthors( Path shortestPath, int degreeSeparation) {
        this.shortestPath = shortestPath;
        DegreeSeparation = degreeSeparation;
    }

    public shortestPathAuthors(Record r){
        this.shortestPath = r.get("path").asPath();
        this.DegreeSeparation = Integer.parseInt(r.get("degreeSeparation").toString());
    }

    public String toJson(){
        return "{\"DegreeSeparation\":"+this.DegreeSeparation+"\"path\":\""+this.shortestPath.toString()+"\"}";
    }

    public Path getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(Path shortestPath) {
        this.shortestPath = shortestPath;
    }

    public int getDegreeSeparation() {
        return DegreeSeparation;
    }

    public void setDegreeSeparation(int degreeSeparation) {
        DegreeSeparation = degreeSeparation;
    }
}
