package gr.server.mongo.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class LongTypeAdapter extends TypeAdapter<Long> {
    @Override
    public void write(JsonWriter out, Long value) throws IOException {
        out.value(value);
    }

    @Override
    public Long read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.BEGIN_OBJECT) {
            // Parse the object containing $numberLong
            in.beginObject();
            String name = in.nextName();
            Long value = null;
            if ("$numberLong".equals(name)) {
                value = Long.parseLong(in.nextString());
            }
            in.endObject();
            return value;
        } else if (token == JsonToken.NUMBER) {
            return in.nextLong();
        } else {
            throw new JsonSyntaxException("Expected a long but was " + token);
        }
    }
}

