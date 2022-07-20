package ru.whiteroomlz.mapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс, предназначенный для хранения классов, аннотированных @Exported.
 */
public final class JsonStructure extends JsonObject {
    private final Map<String, JsonObject> structureMap;

    public Map<String, JsonObject> getStructureMap() {
        return structureMap;
    }

    public JsonStructure(List<String> keys, List<? extends JsonObject> values) {
        long pairs_count;
        if ((pairs_count = keys.size()) != values.size()) {
            throw new IllegalArgumentException("The number of keys and JSON objects must be the same.");
        }

        structureMap = new LinkedHashMap<>();
        for (int item_index = 0; item_index < pairs_count; item_index++) {
            structureMap.put(keys.get(item_index), values.get(item_index));
        }
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonStructure jsonStructure = new JsonStructure("{\"name\":\"Paul\",\"class\":6,\"marks\":[4,5,5,4,2,3,4,4,4]}");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий строковый объект, представимый в виде JSON-Structure.
     *                     Должен быть обрамлён фигурными скобками '{', '}'.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    JsonStructure(String jsonDocument) throws JsonParseException {
        // Проверка на принадлежность к типу JsonStructure.
        if (jsonDocument.startsWith("{") && jsonDocument.endsWith("}")) {
            jsonDocument = jsonDocument.substring(1, jsonDocument.length() - 1);
        } else {
            throw new IncorrectStructureException("The structure must start with '{' and end with '}' characters");
        }

        structureMap = new LinkedHashMap<>();

        if (jsonDocument.length() == 0) {
            return;
        }

        // С помощью стека проверяется корректность скобочной последовательности.
        Stack<Character> stack = new Stack<>();

        char bracket;
        int sequenceStart = 0, sequenceEnd;
        for (int char_index = 0; char_index < jsonDocument.length(); char_index++) {
            switch (jsonDocument.charAt(char_index)) {
                case '{':
                    stack.push('{');
                    break;
                case '[':
                    stack.push('[');
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
                        addKeyValuePair(jsonDocument.substring(sequenceStart, sequenceEnd));
                        sequenceStart = char_index + 1;
                    }
                    break;
            }
        }

        if (stack.empty()) {
            addKeyValuePair(jsonDocument.substring(sequenceStart));
        } else {
            String exceptionMessage = String.format(
                    IncorrectStructureException.NOT_ALL_BRACKETS_CLOSED_EXCEPTION_MESSAGE,
                    stack.peek()
            );
            throw new IncorrectStructureException(exceptionMessage);
        }
    }

    /**
     * Из выделенного фрагмента извлекается пара ключ-значение и включается в поле collection.
     *
     * @param substring выделенный фрагмент.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    private void addKeyValuePair(String substring) throws JsonParseException {
        // Все пары ключ-значение хранятся в виде паттерна: "ключ":значение.
        Pattern keyPattern = Pattern.compile("\"([^\"]+)\":(.*)");

        String key;
        JsonObject value;

        Matcher matcher = keyPattern.matcher(substring);
        if (!matcher.matches()) {
            String exceptionMessage = String.format(
                    "The substring %s should starts with the template pair \"key\":value, but the key was not found.",
                    substring
            );
            throw new JsonParseException(exceptionMessage);
        } else {
            key = matcher.group(1);
            substring = matcher.group(2);
        }
        value = JsonMapper.parseDocument(substring);

        structureMap.put(key, value);
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
        structureMap.forEach((key, value) -> stringJoiner.add("\"" + key + "\"" + ":" +
                (value == null ? null : value.toString())));

        return stringJoiner.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JsonStructure that)) {
            return false;
        }

        return Objects.equals(structureMap, that.structureMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structureMap);
    }
}
