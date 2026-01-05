package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

public class ShortestPathSerializer extends StdSerializer<ShortestPathAuthors> {
  public ShortestPathSerializer() {
    this(null);
  }

  public ShortestPathSerializer(Class<ShortestPathAuthors> t) {
    super(t);
  }

  @Override
  public void serialize(ShortestPathAuthors spa, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeStartObject();

    gen.writeArrayFieldStart("nodes");
    for (Node node : spa.getShortestPath().nodes()) {
      gen.writeStartObject();
      gen.writeStringField("elementId", node.elementId());
      gen.writeStringField("type", node.hasLabel("Author") ? "Author" : "Paper");
      gen.writeStringField(
          "displayName",
          node.hasLabel("Author") ? node.get("name").asString() : node.get("title").asString());
      gen.writeEndObject();
    }
    gen.writeEndArray();

    gen.writeArrayFieldStart("relationships");
    for (Relationship rel : spa.getShortestPath().relationships()) {
      gen.writeStartObject();
      gen.writeStringField("type", rel.type());
      gen.writeStringField("startNodeId", rel.startNodeElementId());
      gen.writeStringField("endNodeId", rel.endNodeElementId());
      gen.writeEndObject();
    }
    gen.writeEndArray();

    gen.writeNumberField("degreeSeparation", spa.getDegreeSeparation());
    gen.writeEndObject();
  }
}
