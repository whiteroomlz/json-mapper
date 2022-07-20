package ru.whiteroomlz.mapper;

/**
 * Класс, предназначенный для хранения строковых объектов.
 */
public final class JsonString extends JsonObject {
    public JsonString(String value) {
        this.value = value;
    }
}
