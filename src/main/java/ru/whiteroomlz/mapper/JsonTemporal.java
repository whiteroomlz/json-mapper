package ru.whiteroomlz.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс, предназначенный для хранения LocalDate, LocalTime и LocalDateTime объектов.
 */
public final class JsonTemporal extends JsonObject {
    /**
     * Паттерн, переданный через аннотацию @DateFormat.
     */
    private final String pattern;

    private static final Pattern defaultDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern defaultTimePattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2}(.\\d)*");

    public JsonTemporal(Temporal temporal) {
        this.value = temporal;
        pattern = null;
    }

    public JsonTemporal(Temporal temporal, String pattern) {
        this.value = temporal;
        this.pattern = pattern;
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonTemporal jsonTemporal = new JsonTemporal("2022-February-05 11:18:47", "uuuu-MMMM-dd HH:mm:ss");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий строковый объект, представимый в виде JSON-Temporal.
     * @param pattern      паттерн, переданный через аннотацию @DateFormat.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    public JsonTemporal(String jsonDocument, String pattern) throws JsonParseException {
        this.pattern = pattern;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            value = formatter.parseBest(jsonDocument, LocalDateTime::from, LocalDate::from, LocalTime::from);
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            String exceptionMessage = String.format(
                    "Impossible to convert the part of specified JSON document:\"%s\" to the JSON temporal value.",
                    jsonDocument
            );
            JsonParseException jsonParseException = new JsonParseException(exceptionMessage);
            jsonParseException.initCause(exception);

            throw jsonParseException;
        }
    }

    /**
     * <p>
     * Пример вызова:
     *
     * <pre>
     * JsonTemporal jsonTemporal = new JsonTemporal("1970-01-01T00:00:00.0");
     * </pre>
     *
     * @param jsonDocument фрагмент JSON документа, содержащий строковый объект,
     *                     подходящий под один из паттернов по умолчанию.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    public JsonTemporal(String jsonDocument) throws JsonParseException {
        pattern = null;

        Matcher dateMatcher = JsonTemporal.defaultDatePattern.matcher(jsonDocument);
        Matcher timeMatcher = defaultTimePattern.matcher(jsonDocument);

        if (dateMatcher.find() && timeMatcher.find()) {
            LocalDate date = LocalDate.parse(dateMatcher.group(0));
            LocalTime time = LocalTime.parse(timeMatcher.group(0));
            value = LocalDateTime.of(date, time);
        } else if (dateMatcher.matches()) {
            value = LocalDate.parse(dateMatcher.group(0));
        } else if (timeMatcher.matches()) {
            value = LocalTime.parse(timeMatcher.group(0));
        } else {
            String exceptionMessage = String.format(
                    "Impossible to convert the part of specified JSON document:\"%s\" to the JSON temporal value.",
                    jsonDocument
            );
            throw new JsonParseException(exceptionMessage);
        }
    }

    @Override
    public String toString() {
        if (pattern != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            if (value instanceof LocalDate date) {
                return "\"" + date.format(formatter) + "\"";
            } else if (value instanceof LocalTime time) {
                return "\"" + time.format(formatter) + "\"";
            } else {
                return "\"" + ((LocalDateTime) value).format(formatter) + "\"";
            }
        } else {
            return super.toString();
        }
    }
}
