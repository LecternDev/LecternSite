package tlm.dev.lectern.site.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonReaderTest {

    private final JsonReader reader = new JsonReader();

    @Test
    void everyKindOfValueIsRead() {
        JsonValue value = reader.read("{\"a\": [1, \"two\", true, false, null], \"b\": {\"c\": 3}}");
        assertEquals(
                new JsonValue.ObjectValue(Map.of(
                        "a", new JsonValue.ArrayValue(List.of(
                                new JsonValue.NumberValue("1"),
                                new JsonValue.StringValue("two"),
                                new JsonValue.BooleanValue(true),
                                new JsonValue.BooleanValue(false),
                                new JsonValue.NullValue())),
                        "b", new JsonValue.ObjectValue(Map.of("c", new JsonValue.NumberValue("3"))))),
                value);
    }

    @Test
    void numbersKeepTheirWrittenText() {
        JsonValue.ObjectValue value = (JsonValue.ObjectValue) reader.read("{\"x\": 0.0, \"y\": 1e3, \"z\": 1073742162}");
        assertEquals(new JsonValue.NumberValue("0.0"), value.members().get("x"));
        assertEquals(new JsonValue.NumberValue("1e3"), value.members().get("y"));
        assertEquals(new JsonValue.NumberValue("1073742162"), value.members().get("z"));
    }

    @Test
    void nonAsciiTextIsKept() {
        String accented = "caf" + (char) 233;
        JsonValue.ObjectValue value = (JsonValue.ObjectValue) reader.read("{\"name\": \"" + accented + "\"}");
        assertEquals(new JsonValue.StringValue(accented), value.members().get("name"));
    }

    @Test
    void duplicateMemberIsRejected() {
        JsonSyntaxException failure = assertThrows(JsonSyntaxException.class, () -> reader.read("{\"a\": 1, \"a\": 2}"));
        assertTrue(failure.getMessage().contains("Duplicate member 'a'"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "{", "[1, 2", "{\"a\": }", "{\"a\": 1} {}", "[1] x", "{'a': 1}", "{\"a\": NaN}"})
    void malformedDocumentIsRejected(String text) {
        assertThrows(JsonSyntaxException.class, () -> reader.read(text));
    }

    @Test
    void errorNamesTheLine() {
        JsonSyntaxException failure = assertThrows(JsonSyntaxException.class, () -> reader.read("{\n  \"a\": ,\n}"));
        assertTrue(failure.getMessage().contains("line 2"), failure.getMessage());
    }

    @Test
    void numberShapeIsChecked() {
        assertThrows(IllegalArgumentException.class, () -> new JsonValue.NumberValue("01"));
        assertThrows(IllegalArgumentException.class, () -> new JsonValue.NumberValue("NaN"));
    }
}
