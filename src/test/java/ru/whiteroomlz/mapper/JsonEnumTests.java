package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonEnumTests {
    enum SomeEnum {
        LOREM_IPSUM,
        DOLOR_SIT_AMET
    }

    @Test
    void toString_ShouldEqualsWithExpected() {
        JsonEnum jsonEnum = new JsonEnum(SomeEnum.LOREM_IPSUM);
        String expected = "\"lorem ipsum\"";

        Assertions.assertEquals(jsonEnum.toString(), expected);
    }
}
