package ru.whiteroomlz.mapper;

import java.util.*;

/**
 * Класс, предназначенный для хранения классов, реализующих интерфейс List или Set.
 */
public final class JsonCollection extends JsonObject {
    final List<? super JsonObject> collection;

    public JsonCollection(Collection<? extends JsonObject> jsonObjects) {
        collection = new ArrayList<>(jsonObjects);
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonCollection jsonCollection = new JsonCollection("[[0],[0,1,2],[]]");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий строковый объект, представимый в виде JSON-Collection.
     *                     Должен быть обрамлён квадратными скобками '[', ']'.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    public JsonCollection(String jsonDocument) throws JsonParseException {
        // Проверка на принадлежность к типу JsonCollection.
        if (jsonDocument.startsWith("[") && jsonDocument.endsWith("]")) {
            jsonDocument = jsonDocument.substring(1, jsonDocument.length() - 1);
        } else {
            throw new IncorrectStructureException("The collection must start with '[' and end with ']' characters");
        }

        collection = new ArrayList<>();

        if (jsonDocument.length() == 0) {
            return;
        }

        // С помощью стека проверяется корректность скобочной последовательности.
        Stack<Character> stack = new Stack<>();

        char bracket;
        int sequenceStart = 0, sequenceEnd;
        for (int char_index = 0; char_index < jsonDocument.length(); char_index++) {
            switch (jsonDocument.charAt(char_index)) {
                case '[':
                    stack.push('[');
                    break;
                case '{':
                    stack.push('{');
                    break;
                case ']':
                    if (stack.empty()) {
                        throw new IncorrectStructureException("Unexpected bracket: ]", char_index);
                    }
                    if ((bracket = stack.pop()) != '[') {
                        String exceptionMessage = String.format(
                                IncorrectStructureException.BRACKETS_CLOSING_ORDER_EXCEPTION_MESSAGE,
                                bracket
                        );
                        throw new IncorrectStructureException(exceptionMessage, char_index);
                    }
                    break;
                case '}':
                    if (stack.empty()) {
                        throw new IncorrectStructureException("Unexpected bracket: }", char_index);
                    }
                    if ((bracket = stack.pop()) != '{') {
                        String exceptionMessage = String.format(
                                IncorrectStructureException.BRACKETS_CLOSING_ORDER_EXCEPTION_MESSAGE,
                                bracket
                        );
                        throw new IncorrectStructureException(exceptionMessage, char_index);
                    }
                    break;
                // Разделитель элементов в последовательном представлении коллекции.
                // Происходит обработка выделенного фрагмента.
                case ',':
                    if (stack.empty()) {
                        sequenceEnd = char_index;
                        if (sequenceStart == sequenceEnd) {
                            throw new IncorrectStructureException("Duplicate separators detected.", char_index);
                        }
                        collection.add(JsonMapper.parseDocument(jsonDocument.substring(sequenceStart, sequenceEnd)));
                        sequenceStart = char_index + 1;
                    }
                    break;
            }
        }

        if (stack.empty()) {
            collection.add(JsonMapper.parseDocument(jsonDocument.substring(sequenceStart)));
        } else {
            String exceptionMessage = String.format(
                    IncorrectStructureException.NOT_ALL_BRACKETS_CLOSED_EXCEPTION_MESSAGE,
                    stack.peek()
            );
            throw new IncorrectStructureException(exceptionMessage);
        }
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
        collection.forEach(value -> stringJoiner.add(value == null ? null : value.toString()));

        return stringJoiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonCollection that)) {
            return false;
        }

        return Objects.equals(collection, that.collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), collection);
    }
}
