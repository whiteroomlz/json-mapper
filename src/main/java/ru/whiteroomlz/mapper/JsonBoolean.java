package ru.whiteroomlz.mapper;

/**
 * Класс, предназначенный для хранения примитива boolean и его обёрточного класса Boolean.
 */
final class JsonBoolean extends JsonPrimitive {
    public JsonBoolean(Boolean value) {
        this.value = value;
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonBoolean jsonBoolean = new JsonBoolean("true");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий примитив, представимый в виде JSON-Boolean.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    public JsonBoolean(String jsonDocument) throws JsonParseException {
        if ("true".equalsIgnoreCase(jsonDocument) || "false".equalsIgnoreCase(jsonDocument)) {
            value = Boolean.parseBoolean(jsonDocument);
        } else {
            String exceptionMessage = String.format(
                    "Impossible to convert the part of specified JSON document:\"%s\" to the boolean value.",
                    jsonDocument
            );
            throw new JsonParseException(exceptionMessage);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonBoolean jsonBoolean) {
            if (obj == this) {
                return true;
            }

            return ((Boolean) value).booleanValue() == ((Boolean) jsonBoolean.value).booleanValue();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
