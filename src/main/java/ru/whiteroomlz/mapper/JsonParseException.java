package ru.whiteroomlz.mapper;

import java.text.ParseException;

/**
 * Исключение, выбрасываемое при обнаружении ошибки в JSON документе.
 */
public class JsonParseException extends ParseException {
    /**
     * @param message сообщение о деталях ошибки.
     * @param offset  позиция ошибки в тексте.
     */
    public JsonParseException(String message, int offset) {
        super(message, offset);
    }

    /**
     * @param message сообщение о деталях ошибки.
     */
    public JsonParseException(String message) {
        this(message, 0);
    }
}
