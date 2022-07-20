package ru.whiteroomlz.mapper;

/**
 * Класс, предназначенный для хранения примитива char и его обёрточного класса Character.
 */
final class JsonCharacter extends JsonPrimitive {
    public JsonCharacter(Character value) {
        this.value = value;
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonCharacter jsonCharacter = new JsonCharacter("//n");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий примитив, представимый в виде JSON-Character.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    public JsonCharacter(String jsonDocument) throws JsonParseException {
        if (jsonDocument.length() == 1) {
            value = jsonDocument.charAt(0);
        } else if (jsonDocument.startsWith("\\")) {

            // Отдельная обработка эскейп-последовательностей, поддерживаемых виртуальной машиной Java.
            switch (jsonDocument.charAt(1)) {
                case 't' -> value = '\t';
                case 'b' -> value = '\b';
                case 'n' -> value = '\n';
                case 'r' -> value = '\r';
                case 'f' -> value = '\f';
            }
        } else {
            String exceptionMessage = String.format(
                    "Impossible to convert the part of specified JSON document:\"%s\" to the character value.",
                    jsonDocument
            );
            throw new JsonParseException(exceptionMessage);
        }
    }

    @Override
    public String toString() {
        return switch ((Character) value) {
            case '\t' -> "\\t";
            case '\b' -> "\\b";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\f' -> "\\f";
            default -> super.toString();
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonCharacter jsonCharacter) {
            if (obj == this) {
                return true;
            }

            return ((Character) value).charValue() == ((Character) jsonCharacter.value).charValue();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
