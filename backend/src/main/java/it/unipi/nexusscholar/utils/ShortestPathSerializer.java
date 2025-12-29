package it.unipi.nexusscholar.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class ShortestPathSerializer extends StdSerializer<ShortestPathAuthors> {
  public ShortestPathSerializer() {
    this(null);
  }

  public ShortestPathSerializer(Class<ShortestPathAuthors> t) {
    super(t);
  }

  @Override
  public void serialize(
      ShortestPathAuthors spa, JsonGenerator jsonGenerator, SerializerProvider serializer)
      throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("path_nodes", spa.getShortestPath().nodes().toString());
    jsonGenerator.writeStringField(
        "relationships", spa.getShortestPath().relationships().toString());
    jsonGenerator.writeNumberField("degreeSeparation",spa.getDegreeSeparation());
    jsonGenerator.writeEndObject();
  }
}
