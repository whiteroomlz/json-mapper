package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.ParseException;

public class JsonNumberTests {
    @Test
    void equals_ShouldReturnTrueForEqualNumbers() {
        JsonNumber jsonNumber = new JsonNumber(0);
        JsonNumber other = new JsonNumber(0);

        Assertions.assertEquals(jsonNumber, other);
    }

    @Test
    void equals_ShouldReturnTrueForDifferentWrappers() {
        JsonNumber jsonNumber = new JsonNumber(0);
        JsonNumber other = new JsonNumber((byte) 0);

        Assertions.assertNotEquals(jsonNumber, other);
    }

    @Test
    void equals_ShouldReturnFalseForNotEqualNumbers() {
        JsonNumber jsonNumber = new JsonNumber(0L);
        JsonNumber other = new JsonNumber(0.0);

        Assertions.assertNotEquals(other, jsonNumber);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "null", ""})
    void incorrectDocumentInConstructor_ShouldThrowJsonParseException(String jsonDocument) {
        Assertions.assertThrows(JsonParseException.class, () -> new JsonNumber(jsonDocument));
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1L, 1", "2.0, 2.0"})
    void correctDocumentInConstructor_ShouldReturnCorrectJsonCharacter(String jsonDocument, String expected)
            throws ParseException {
        JsonNumber jsonNumber = new JsonNumber(jsonDocument);
        Assertions.assertEquals(expected, jsonNumber.value.toString());
    }
}
