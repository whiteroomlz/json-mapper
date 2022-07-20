package ru.whiteroomlz.mapper;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Класс, предназначенный для хранения числовых примитивов и их обёрточных классов.
 */
final class JsonNumber extends JsonPrimitive {
    public JsonNumber(Number value) {
        this.value = value;
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonNumber jsonNumber = new JsonNumber("0");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий примитив, представимый в виде JSON-Number.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    public JsonNumber(String jsonDocument) throws JsonParseException {
        try {
            // Предполагается, что разделителем является точка.
            if (jsonDocument.contains(".")) {
                value = Double.parseDouble(jsonDocument);
            } else {
                value = NumberFormat.getInstance().parse(jsonDocument);
            }
        } catch (ParseException exception) {
            String exceptionMessage = String.format(
                    "Impossible to convert the part of specified JSON document:\"%s\" to the numeric value.",
                    jsonDocument
            );
            JsonParseException jsonParseException = new JsonParseException(exceptionMessage);
            jsonParseException.initCause(exception);

            throw jsonParseException;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonNumber jsonNumber) {
            if (obj == this) {
                return true;
            }

            return value.equals(jsonNumber.value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
