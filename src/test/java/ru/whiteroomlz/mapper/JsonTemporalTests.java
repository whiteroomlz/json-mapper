package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class JsonTemporalTests {
    @ParameterizedTest
    @ValueSource(strings = {"1970.01.01", "01-01-1970", "19:35", "4 a.m."})
    void incorrectDocumentInConstructor_ShouldThrowJsonParseException(String jsonDocument) {
        Assertions.assertThrows(JsonParseException.class, () -> new JsonTemporal(jsonDocument));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1970-01-01"})
    void correctDateDocumentInConstructor_ShouldReturnCorrespondingTemporalValue(String jsonDocument)
            throws JsonParseException {
        JsonTemporal jsonTemporal = new JsonTemporal(jsonDocument);
        Assertions.assertEquals(LocalDate.parse(jsonDocument), jsonTemporal.value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00:00.0"})
    void correctTimeDocumentInConstructor_ShouldReturnCorrespondingTemporalValue(String jsonDocument)
            throws JsonParseException {
        JsonTemporal jsonTemporal = new JsonTemporal(jsonDocument);
        Assertions.assertEquals(LocalTime.parse(jsonDocument), jsonTemporal.value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1970-01-01T00:00:00.0"})
    void correctDateTimeDocumentInConstructor_ShouldReturnCorrespondingTemporalValue(String jsonDocument)
            throws JsonParseException {
        JsonTemporal jsonTemporal = new JsonTemporal(jsonDocument);
        Assertions.assertEquals(LocalDateTime.parse(jsonDocument), jsonTemporal.value);
    }

    @ParameterizedTest
    @CsvSource({
            "2022-February-05 11:18:47, uuuu-MMMM-dd HH:mm:ss"
    })
    void correctDocumentInFormatConstructor_ShouldReturnCorrespondingTemporalValue(String jsonDocument, String format)
            throws JsonParseException {
        JsonTemporal jsonTemporal = new JsonTemporal(jsonDocument, format);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

        Assertions.assertEquals(LocalDateTime.parse(jsonDocument, formatter), jsonTemporal.value);
    }
}
