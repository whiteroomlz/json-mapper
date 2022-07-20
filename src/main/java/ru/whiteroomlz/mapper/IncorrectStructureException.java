package ru.whiteroomlz.mapper;

/**
 * Исключение, выбрасываемое при обнаружении ошибки в скобочной последовательности JSON документа.
 */
public class IncorrectStructureException extends JsonParseException {
    static final String BRACKETS_CLOSING_ORDER_EXCEPTION_MESSAGE =
            "It was expected that the bracket \"%c\" would be closed before closing the previous brackets.";
    static final String NOT_ALL_BRACKETS_CLOSED_EXCEPTION_MESSAGE =
            "The document contains open bracket \"%c\" that have not been closed.";

    /**
     * @param message сообщение о деталях ошибки.
     * @param offset  позиция ошибки в тексте.
     */
    public IncorrectStructureException(String message, int offset) {
        super(message, offset);
    }

    /**
     * @param message сообщение о деталях ошибки.
     */
    public IncorrectStructureException(String message) {
        super(message);
    }
}
