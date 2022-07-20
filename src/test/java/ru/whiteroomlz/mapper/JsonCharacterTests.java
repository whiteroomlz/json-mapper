package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonCharacterTests {
    private final int TO_STRING_TEST_ORDER = 0;
    private final int CORRECT_DOCUMENT_IN_CONSTRUCTOR_TEST_ORDER = 1;

    @Test
    void equals_shouldReturnTrueForEqualCharacters() {
        JsonCharacter jsonCharacter = new JsonCharacter('a');
        JsonCharacter other = new JsonCharacter('a');

        Assertions.assertEquals(jsonCharacter, other);
    }

    @Test
    void equals_shouldReturnFalseForNotEqualCharacters() {
        JsonCharacter jsonCharacter = new JsonCharacter('a');
        JsonCharacter other = new JsonCharacter('b');

        Assertions.assertNotEquals(jsonCharacter, other);
    }

    @Test
    @Order(TO_STRING_TEST_ORDER)
    void toString_ShouldEqualsWithExpected() {
        JsonCharacter jsonCharacter = new JsonCharacter('\n');
        String expected = "\\n";

        Assertions.assertEquals(jsonCharacter.toString(), expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {" -", "...", "lorem ipsum", ""})
    void incorrectDocumentInConstructor_ShouldThrowJsonParseException(String jsonDocument) {
        Assertions.assertThrows(JsonParseException.class, () -> new JsonBoolean(jsonDocument));
    }

    @ParameterizedTest
    @Order(CORRECT_DOCUMENT_IN_CONSTRUCTOR_TEST_ORDER)
    @ValueSource(strings = {"'", " ", "\\n", "a"})
    void correctDocumentInConstructor_ShouldReturnCorrectJsonCharacter(String jsonDocument) throws JsonParseException {
        JsonCharacter jsonBoolean = new JsonCharacter(jsonDocument);
        Assertions.assertEquals(jsonDocument, jsonBoolean.toString());
    }
}
