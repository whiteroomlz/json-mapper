package ru.whiteroomlz.mapper;

/**
 * JSON-представление примитивов, а также их обёрточных классов.
 */
public class JsonPrimitive extends JsonObject {
    JsonPrimitive(Object value) {
        super(value);
    }

    protected JsonPrimitive() {
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
