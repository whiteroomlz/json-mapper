package ru.whiteroomlz.mapper;

import java.util.Locale;

/**
 * Класс, предназначенный для хранения enum объектов.
 */
public class JsonEnum extends JsonObject {
    public JsonEnum(Enum<?> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "\"" + value.toString().replaceAll("_", " ").toLowerCase(Locale.ROOT) + "\"";
    }
}
