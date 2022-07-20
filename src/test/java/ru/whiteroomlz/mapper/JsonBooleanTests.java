package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class JsonBooleanTests {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void equals_ShouldReturnTrueForEqualBooleans(boolean value) {
        JsonBoolean jsonBoolean = new JsonBoolean(value);
        JsonBoolean other = new JsonBoolean(value);

        Assertions.assertEquals(jsonBoolean, other);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void equals_ShouldReturnFalseForNotEqualBooleans(boolean value) {
        JsonBoolean jsonBoolean = new JsonBoolean(value);
        JsonBoolean other = new JsonBoolean(!value);

        Assertions.assertNotEquals(other, jsonBoolean);
    }

    @ParameterizedTest
    @ValueSource(strings = {" true", ":false", "lorem ipsum", ""})
    void incorrectDocumentInConstructor_ShouldThrowJsonParseException(String jsonDocument) {
        Assertions.assertThrows(JsonParseException.class, () -> new JsonBoolean(jsonDocument));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "True", "False"})
    void correctDocumentInConstructor_ShouldReturnCorrectJsonBoolean(String jsonDocument) throws JsonParseException {
        JsonBoolean jsonBoolean = new JsonBoolean(jsonDocument);
        Assertions.assertEquals(jsonDocument.toLowerCase(), jsonBoolean.toString());
    }
}
