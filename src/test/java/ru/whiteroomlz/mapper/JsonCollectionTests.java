package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonCollectionTests {
    private final int TO_STRING_TEST_ORDER = 0;
    private final int CORRECT_DOCUMENT_IN_CONSTRUCTOR_TEST_ORDER = 1;

    @Test
    @Order(TO_STRING_TEST_ORDER)
    void toString_ShouldEqualsWithExpected() {
        JsonCollection collection1 = new JsonCollection(List.of(new JsonNumber(0)));
        JsonCollection collection2 = new JsonCollection(Stream.of(0, 1, 2).map(JsonNumber::new).toList());
        JsonCollection collection3 = new JsonCollection(List.of());

        JsonCollection collection = new JsonCollection(List.of(collection1, collection2, collection3));
        String expected = "[[0],[0,1,2],[]]";

        Assertions.assertEquals(collection.toString(), expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "][0]],[0,1,2],[]]",
            "[[0]],[0,1,2],[][",
            "[[0]],[0,1,2],[]]",
            "[[0],[[0,1,2],[]]",
            "[[0,][0,1,2],[]]",
            "[[0,[0,1,2],[]]",
            "[[0],[0,1,2][,]]",
            "[[0],[0,1,2],[]",
            "[[0],,[0,1,2],[]]",
            "[[0][0,1,2],[]]",
    })
    void incorrectDocumentInConstructor_ShouldThrowIncorrectStructureException(String jsonDocument) {
        Assertions.assertThrows(IncorrectStructureException.class, () -> new JsonCollection(jsonDocument));
    }

    @ParameterizedTest
    @Order(CORRECT_DOCUMENT_IN_CONSTRUCTOR_TEST_ORDER)
    @ValueSource(strings = {
            "[[0],[0,1,2],[]]",
            "[[[[0,1,2]]]]",
            "[]",
            "[[[0, 1],[1, 0]], [[],[],[]]]"
    })
    void correctDocumentInConstructor_ShouldReturnCorrectJsonCollection(String jsonDocument) throws JsonParseException {
        JsonCollection jsonCollection = new JsonCollection(jsonDocument);
        Assertions.assertEquals(jsonCollection.toString(), jsonDocument);
    }
}
