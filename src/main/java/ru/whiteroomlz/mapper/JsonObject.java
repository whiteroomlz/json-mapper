package ru.whiteroomlz.mapper;

import java.util.Objects;

/**
 * JSON-представление любых объектов, наследующихся от Object.
 */
public class JsonObject {
    protected Object value;

    JsonObject(Object value) {
        this.value = value;
    }

    protected JsonObject() {
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof JsonObject that)) {
            return false;
        }

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
