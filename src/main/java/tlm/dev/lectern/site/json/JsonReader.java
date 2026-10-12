package tlm.dev.lectern.site.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.json.JsonFactory;

public final class JsonReader {

    private final JsonFactory factory = JsonFactory.builder().build();

    public JsonValue read(String text) {
        try (JsonParser parser = factory.createParser(ObjectReadContext.empty(), text)) {
            JsonToken first = parser.nextToken();
            if (first == null) {
                throw new JsonSyntaxException("The document is empty");
            }
            JsonValue value = readValue(parser, first);
            if (parser.nextToken() != null) {
                throw new JsonSyntaxException("Unexpected content after the document" + locationOf(parser));
            }
            return value;
        } catch (JacksonException e) {
            throw new JsonSyntaxException(e.getOriginalMessage() + locationOf(e.getLocation()), e);
        }
    }

    private JsonValue readValue(JsonParser parser, JsonToken token) {
        return switch (token) {
            case START_OBJECT -> readObject(parser);
            case START_ARRAY -> readArray(parser);
            case VALUE_STRING -> new JsonValue.StringValue(parser.getString());
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> new JsonValue.NumberValue(parser.getString());
            case VALUE_TRUE -> new JsonValue.BooleanValue(true);
            case VALUE_FALSE -> new JsonValue.BooleanValue(false);
            case VALUE_NULL -> new JsonValue.NullValue();
            default -> throw new JsonSyntaxException("Unexpected token " + token + locationOf(parser));
        };
    }

    private JsonValue.ObjectValue readObject(JsonParser parser) {
        Map<String, JsonValue> members = new LinkedHashMap<>();
        JsonToken token = parser.nextToken();
        while (token == JsonToken.PROPERTY_NAME) {
            String name = parser.currentName();
            JsonToken valueToken = parser.nextToken();
            if (valueToken == null) {
                throw new JsonSyntaxException("Missing value for member '" + name + "'" + locationOf(parser));
            }
            if (members.putIfAbsent(name, readValue(parser, valueToken)) != null) {
                throw new JsonSyntaxException("Duplicate member '" + name + "'" + locationOf(parser));
            }
            token = parser.nextToken();
        }
        if (token != JsonToken.END_OBJECT) {
            throw new JsonSyntaxException("Unterminated object" + locationOf(parser));
        }
        return new JsonValue.ObjectValue(members);
    }

    private JsonValue.ArrayValue readArray(JsonParser parser) {
        List<JsonValue> elements = new ArrayList<>();
        JsonToken token = parser.nextToken();
        while (token != null && token != JsonToken.END_ARRAY) {
            elements.add(readValue(parser, token));
            token = parser.nextToken();
        }
        if (token == null) {
            throw new JsonSyntaxException("Unterminated array" + locationOf(parser));
        }
        return new JsonValue.ArrayValue(elements);
    }

    private static String locationOf(JsonParser parser) {
        return locationOf(parser.currentTokenLocation());
    }

    private static String locationOf(TokenStreamLocation location) {
        if (location == null || location.getLineNr() < 0) {
            return "";
        }
        return " (line " + location.getLineNr() + ", column " + location.getColumnNr() + ")";
    }
}
