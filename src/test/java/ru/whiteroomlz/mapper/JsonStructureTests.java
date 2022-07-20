package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

public class JsonStructureTests {
    private final int TO_STRING_TEST_ORDER = 0;
    private final int CORRECT_DOCUMENT_IN_CONSTRUCTOR_TEST_ORDER = 1;

    @Test
    @Order(TO_STRING_TEST_ORDER)
    void toString_ShouldEqualsWithExpected() {
        JsonString name = new JsonString("Paul");
        JsonNumber class_number = new JsonNumber(6);
        JsonCollection marks = new JsonCollection(Stream.of(4, 5, 5, 4, 2, 3, 4, 4, 4).map(JsonNumber::new).toList());

        List<String> keys = List.of("name", "class", "marks");
        List<JsonObject> values = List.of(name, class_number, marks);
        JsonStructure jsonStructure = new JsonStructure(keys, values);

        String expected = "{\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}";

        Assertions.assertEquals(jsonStructure.toString(), expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]",
            "[\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]]",
            "{{\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}",
            "{\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}}",
            "{name:\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}",
            "{\"\"name\"\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}",
            "{\"name\":\"Paul\",,\"marks\":[4,5,5,4,2,3,4,4,4]}",
            "{\"name\":\"Paul\",:\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}",
    })
    void incorrectDocumentInConstructor_ShouldThrowJsonParseException(String jsonDocument) {
        Assertions.assertThrows(JsonParseException.class, () -> new JsonStructure(jsonDocument));
    }

    @ParameterizedTest
    @Order(CORRECT_DOCUMENT_IN_CONSTRUCTOR_TEST_ORDER)
    @ValueSource(strings = {
            "{\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}",
            "{\"oak\":{\"chest\":{\"hare\":{\"duck\":{\"egg\":{\"spine\":\"death of Koshchei\"}}}}}}"
    })
    void correctDocumentInConstructor_ShouldReturnCorrectJsonCollection(String jsonDocument) throws JsonParseException {
        JsonStructure jsonStructure = new JsonStructure(jsonDocument);
        Assertions.assertEquals(jsonStructure.toString(), jsonDocument);
    }
}
