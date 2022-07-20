package ru.whiteroomlz.mapper;

import ru.hse.homework4.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс, объекты которого выполняют функции сериализатора или десериализатора псевдо JSON-файлов.
 * Особенность этих файлов заключается в сериализации char и Character отдельно от String (без обрамления кавычками).
 */
public class JsonMapper implements Mapper {
    /**
     * Выполняет функцию упаковки примитивов в их обёрточные классы.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVES_WRAPPERS;

    static {
        PRIMITIVES_WRAPPERS = new HashMap<>();
        PRIMITIVES_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_WRAPPERS.put(double.class, Double.class);
        PRIMITIVES_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_WRAPPERS.put(short.class, Short.class);
    }

    private final boolean retainIdentity;
    private final IdentityHashMap<Object, JsonStructure> identityHashMap;

    public JsonMapper(boolean retainIdentity) {
        this.retainIdentity = retainIdentity;

        if (retainIdentity) {
            identityHashMap = new IdentityHashMap<>();
        } else {
            identityHashMap = null;
        }
    }

    @Override
    public <T> T readFromString(Class<T> clazz, String input) {
        try {
            JsonObject jsonObject = parseDocument(input);
            if (jsonObject instanceof JsonStructure structure) {
                T object = restoreStructure(clazz, structure);
                if (retainIdentity) {
                    assert identityHashMap != null;
                    identityHashMap.clear();
                }

                return object;
            } else {
                throw new IllegalArgumentException("Class cannot be simple.");
            }
        } catch (JsonParseException exception) {
            throw new IllegalArgumentException("Impossible to parse JSON document", exception);
        }
    }

    @Override
    public <T> T read(Class<T> clazz, InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            T object = readFromString(clazz, jsonDocument);
            if (retainIdentity) {
                assert identityHashMap != null;
                identityHashMap.clear();
            }

            return object;
        }
    }

    @Override
    public <T> T read(Class<T> clazz, File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.UTF_8))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            T object = readFromString(clazz, jsonDocument);
            if (retainIdentity) {
                assert identityHashMap != null;
                identityHashMap.clear();
            }

            return object;
        }
    }

    @Override
    public String writeToString(Object object) {
        JsonStructure jsonStructure = getJsonStructure(object);
        if (retainIdentity) {
            assert identityHashMap != null;
            identityHashMap.clear();
        }

        return jsonStructure.toString();
    }

    @Override
    public void write(Object object, OutputStream outputStream) throws IOException {
        JsonStructure jsonStructure = getJsonStructure(object);
        if (retainIdentity) {
            assert identityHashMap != null;
            identityHashMap.clear();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write(jsonStructure.toString());
        }
    }

    @Override
    public void write(Object object, File file) throws IOException {
        JsonStructure jsonStructure = getJsonStructure(object);
        if (retainIdentity) {
            assert identityHashMap != null;
            identityHashMap.clear();
        }

        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.write(jsonStructure.toString());
        }
    }

    /**
     * Преобразует объект в JsonObject.
     *
     * @param object     сериализуемый объект.
     * @param dateFormat паттерн строкового представления Temporal типов. Используется методом getJsonTemporal.
     * @return JSON-представление переданного объекта.
     */
    private JsonObject getJson(Object object, DateFormat dateFormat) {
        if (object == null) {
            return null;
        } else if (object instanceof Number || object instanceof Boolean || object instanceof Character) {
            return getJsonPrimitive(object);
        } else if (object instanceof LocalDate || object instanceof LocalTime || object instanceof LocalDateTime) {
            return getJsonTemporal((Temporal) object, dateFormat);
        } else if (object instanceof String string) {
            return new JsonString(string);
        } else if (object instanceof Enum<?> value) {
            return new JsonEnum(value);
        } else if (object instanceof Collection<?> boxedCollection) {
            return new JsonCollection(boxedCollection.stream()
                    .map(item -> getJson(item, dateFormat))
                    .collect(Collectors.toList()));
        } else {
            return getJsonStructure(object);
        }
    }

    /**
     * Преобразует примитивный объект или его обёртку в JsonPrimitive.
     *
     * @param object сериализуемый объект.
     * @return JSON-представление переданного объекта.
     */
    private JsonPrimitive getJsonPrimitive(Object object) {
        if (object instanceof Number) {
            return new JsonNumber((Number) object);
        } else if (object instanceof Boolean) {
            return new JsonBoolean((Boolean) object);
        } else {
            return new JsonCharacter((Character) object);
        }
    }

    /**
     * Преобразует LocalDate, LocalTime или LocalDateTime объект в JsonTemporal.
     *
     * @param temporal сериализуемый объект.
     * @return JSON-представление переданного объекта.
     */
    private JsonTemporal getJsonTemporal(Temporal temporal, DateFormat dateFormat) {
        if (dateFormat != null) {
            return new JsonTemporal(temporal, dateFormat.value());
        } else {
            return new JsonTemporal(temporal);
        }
    }

    /**
     * Преобразует объект класса, аннотированного @Exported, в JsonStructure.
     *
     * @param object сериализуемый объект.
     * @return JSON-представление переданного объекта.
     */
    private JsonStructure getJsonStructure(Object object) {
        if (retainIdentity) {
            assert identityHashMap != null;
            if (identityHashMap.containsKey(object)) {
                return identityHashMap.get(object);
            }
        }

        Class<?> clazz = object.getClass();

        if (clazz.isAnnotationPresent(Exported.class)) {
            Exported exported = clazz.getAnnotation(Exported.class);

            List<? extends AnnotatedElement> exportedComponents = getExportedComponents(clazz);

            List<String> keys = new ArrayList<>();
            List<JsonObject> values = new ArrayList<>();
            for (AnnotatedElement annotatedElement : exportedComponents) {
                String key;
                Object value;
                try {
                    if (clazz.isRecord()) {
                        RecordComponent component = (RecordComponent) annotatedElement;
                        key = component.getName();
                        value = component.getAccessor().invoke(object);
                    } else {
                        Field field = (Field) annotatedElement;
                        key = field.getName();
                        value = field.get(object);
                    }
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new IllegalArgumentException("Impossible to get value of object element.", exception);
                }

                if (annotatedElement.isAnnotationPresent(PropertyName.class)) {
                    key = annotatedElement.getAnnotation(PropertyName.class).value()
                            .replaceAll("[\"]", "'");
                }

                if (value == null && exported.nullHandling().equals(NullHandling.EXCLUDE)) {
                    continue;
                }

                DateFormat dateFormat = null;
                if (annotatedElement.isAnnotationPresent(DateFormat.class)) {
                    dateFormat = annotatedElement.getAnnotation(DateFormat.class);
                }

                keys.add(key);
                values.add(getJson(value, dateFormat));
            }

            if (retainIdentity) {
                if (keys.contains("Identity id")) {
                    throw new IllegalArgumentException("PropertyName \"Identity id\" must be unused.");
                }
                keys.add("Identity id");
                values.add(new JsonNumber(identityHashMap.size()));

                JsonStructure jsonStructure = new JsonStructure(keys, values);
                identityHashMap.put(object, jsonStructure);
                return jsonStructure;
            } else {
                return new JsonStructure(keys, values);
            }
        } else {
            throw new IllegalArgumentException("The serializable class must be marked with the @Exported annotation.");
        }
    }

    /**
     * Обрабатывает переданный JSON-документ или его фрагмент.
     *
     * @param jsonDocument JSON-документ или его фрагмент.
     * @return реконструированный на основе переданного документа JSON-Object.
     * @throws JsonParseException если не удалось обработать переданный фрагмент.
     */
    static JsonObject parseDocument(String jsonDocument) throws JsonParseException {
        if (jsonDocument.startsWith("{") && jsonDocument.endsWith("}")) {
            return new JsonStructure(jsonDocument);
        } else if (jsonDocument.startsWith("[") && jsonDocument.endsWith("]")) {
            return new JsonCollection(jsonDocument);
        } else if (!jsonDocument.startsWith("\"") && !jsonDocument.endsWith("\"")) {
            return new JsonPrimitive(jsonDocument);
        } else {
            return new JsonObject(jsonDocument.substring(1, jsonDocument.length() - 1));
        }
    }

    /**
     * Десериализует объект на основе его JSON-представления.
     *
     * @param clazz       представление класса десериализуемого объекта в Java VM.
     * @param jsonObject  JSON-представление объекта.
     * @param genericType типизация дженерика десериализуемого объекта. null в случае, если тип не параметризуем.
     * @param dateFormat  паттерн строкового представления Temporal типов. Используется методом restoreTemporal.
     * @param <T>         тип десериализуемого объекта.
     * @return восстановленный объект типа T.
     * @throws JsonParseException в случае, если не удалось сопоставить JSON-объект со структурой класса clazz.
     */
    private <T> T restoreObject(Class<T> clazz, JsonObject jsonObject, Class<?> genericType, DateFormat dateFormat)
            throws JsonParseException {
        if (jsonObject == null) {
            return null;
        }

        if (jsonObject instanceof JsonCollection jsonCollection) {
            return restoreCollection(clazz, jsonCollection, genericType, dateFormat);
        } else if (jsonObject instanceof JsonStructure jsonStructure) {
            return restoreStructure(clazz, jsonStructure);
        } else if (jsonObject instanceof JsonPrimitive jsonPrimitive) {
            return restorePrimitive(clazz, jsonPrimitive);
        } else {
            if (clazz.isEnum()) {
                return restoreEnum(clazz, jsonObject);
            } else if (Temporal.class.isAssignableFrom(clazz)) {
                return restoreTemporal(clazz, jsonObject, dateFormat);
            } else {
                return clazz.cast(jsonObject.value);
            }
        }
    }

    /**
     * Десериализует примитивный объект на основе его JSON-представления. Подавлены unchecked предупреждения, так как в
     * методе рассматриваются все возможные случаи явного приведения при корректных переданных данных.
     *
     * @param clazz         представление класса десериализуемого объекта в Java VM.
     * @param jsonPrimitive JSON-представление примитивного объекта.
     * @param <T>           тип десериализуемого объекта.
     * @return восстановленный объект типа T.
     * @throws JsonParseException в случае, если не удалось сопоставить JSON-объект со структурой класса clazz.
     */
    @SuppressWarnings("unchecked")
    private <T> T restorePrimitive(Class<T> clazz, JsonPrimitive jsonPrimitive) throws JsonParseException {
        if ("null".equals(jsonPrimitive.value.toString())) {
            return null;
        }

        Class<?> wrapperClazz;
        if (clazz.isPrimitive()) {
            wrapperClazz = PRIMITIVES_WRAPPERS.get(clazz);
        } else {
            wrapperClazz = clazz;
        }

        if (Number.class.isAssignableFrom(wrapperClazz)) {
            JsonNumber number = new JsonNumber(jsonPrimitive.value.toString());

            if (number.value instanceof Double value) {
                if (wrapperClazz == Float.class) {
                    return (T) wrapperClazz.cast(value.floatValue());
                } else {
                    return (T) wrapperClazz.cast(value);
                }
            } else if (number.value instanceof Long value) {
                if (wrapperClazz == Byte.class) {
                    return (T) wrapperClazz.cast(value.byteValue());
                } else if (wrapperClazz == Short.class) {
                    return (T) wrapperClazz.cast(value.shortValue());
                } else if (wrapperClazz == Integer.class) {
                    return (T) wrapperClazz.cast(value.intValue());
                } else {
                    return (T) wrapperClazz.cast(value);
                }
            } else {
                throw new IllegalArgumentException("Unexpected number.");
            }
        } else {
            if (Boolean.class.isAssignableFrom(wrapperClazz)) {
                return (T) wrapperClazz.cast(new JsonBoolean(jsonPrimitive.value.toString()).value);
            } else {
                return (T) wrapperClazz.cast(new JsonCharacter(jsonPrimitive.value.toString()).value);
            }
        }
    }

    /**
     * Десериализует коллекцию на основе её JSON-представления. Подавлены unchecked предупреждения, так как в
     * методе используется явное приведение типов без риска возникновения исключения.
     *
     * @param clazz          представление класса десериализуемого объекта в Java VM.
     * @param jsonCollection JSON-представление перечислимой коллекции.
     * @param genericType    тип, которым параметризована коллекция.
     * @param dateFormat     паттерн строкового представления Temporal типов. Используется методом restoreObject.
     * @param <T>            тип десериализуемого объекта.
     * @return восстановленная коллекция типа T.
     */
    @SuppressWarnings("unchecked")
    private <T> T restoreCollection(Class<T> clazz, JsonCollection jsonCollection, Class<?> genericType,
                                    DateFormat dateFormat) {
        Constructor<?> defaultConstructor;

        if (clazz.getDeclaredConstructors().length == 0) {
            if (List.class.isAssignableFrom(clazz)) {
                defaultConstructor = getClassDefaultConstructor(ArrayList.class);
            } else if (Set.class.isAssignableFrom(clazz)) {
                defaultConstructor = getClassDefaultConstructor(HashSet.class);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            defaultConstructor = getClassDefaultConstructor(clazz);
        }

        try {
            T instance = clazz.cast(defaultConstructor.newInstance());
            if (instance instanceof Collection collection) {
                for (var item : jsonCollection.collection) {
                    collection.add(restoreObject(genericType, (JsonObject) item, null, dateFormat));
                }
                return clazz.cast(instance);
            } else {
                throw new IllegalArgumentException(String.format("Class %s is not a Collection", clazz.getName()));
            }
        } catch (ReflectiveOperationException | JsonParseException exception) {
            throw new RuntimeException(
                    String.format("Impossible to create a new instance of %s", clazz.getName()),
                    exception
            );
        }
    }

    /**
     * Десериализует объект класса, аннотированного @Exported.
     *
     * @param clazz         представление класса десериализуемого объекта в Java VM.
     * @param jsonStructure JSON-представление объекта.
     * @param <T>           тип десериализуемого объекта.
     * @return восстановленный объект типа T.
     */
    private <T> T restoreStructure(Class<T> clazz, JsonStructure jsonStructure) {
        if (retainIdentity) {
            assert identityHashMap != null;
            for (Object key : identityHashMap.keySet()) {
                if (identityHashMap.get(key).equals(jsonStructure)) {
                    return clazz.cast(key);
                }
            }

        }

        if (clazz.isAnnotationPresent(Exported.class)) {
            Exported exported = clazz.getAnnotation(Exported.class);

            T object;
            if (clazz.isRecord()) {
                object = restoreRecordObject(clazz, jsonStructure, exported);
            } else {
                object = restoreClassObject(clazz, jsonStructure, exported);
            }

            if (retainIdentity) {
                identityHashMap.put(object, jsonStructure);
            }
            return object;
        } else {
            throw new IllegalArgumentException("The serializable class must be marked with the @Exported annotation.");
        }
    }

    /**
     * Десериализует объект record-класса.
     *
     * @param clazz         представление класса десериализуемого объекта в Java VM.
     * @param jsonStructure JSON-представление объекта.
     * @param exported      @Exported аннотация, содержащая метаинформацию о процессе десериализации.
     * @param <T>           тип десериализуемого объекта.
     * @return восстановленный объект типа T.
     */
    private <T> T restoreRecordObject(Class<T> clazz, JsonStructure jsonStructure, Exported exported) {
        Constructor<?> constructor = getRecordConstructor(clazz);

        List<? extends AnnotatedElement> exportedComponents = getExportedComponents(clazz);
        Map<String, Object> recordParameters = new LinkedHashMap<>();

        try {
            for (AnnotatedElement annotatedElement : exportedComponents) {
                String key;
                Class<?> genericType = null;

                RecordComponent component = (RecordComponent) annotatedElement;
                if (component.getGenericType() instanceof ParameterizedType parameterizedType) {
                    genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }
                key = component.getName();

                if (annotatedElement.isAnnotationPresent(PropertyName.class)) {
                    key = annotatedElement.getAnnotation(PropertyName.class).value()
                            .replaceAll("[\"]", "'");
                }

                DateFormat dateFormat = null;
                if (annotatedElement.isAnnotationPresent(DateFormat.class)) {
                    dateFormat = annotatedElement.getAnnotation(DateFormat.class);
                }

                if (!jsonStructure.getStructureMap().containsKey(key)) {
                    if (exported.unknownPropertiesPolicy() == UnknownPropertiesPolicy.FAIL) {
                        throw new IllegalArgumentException();
                    } else {
                        continue;
                    }
                }

                Object value = restoreObject(
                        component.getType(),
                        jsonStructure.getStructureMap().get(key),
                        genericType,
                        dateFormat
                );

                recordParameters.put(component.getName(), value);
            }

            if (recordParameters.size() != constructor.getParameterCount()) {
                throw new IllegalArgumentException();
            }

            return clazz.cast(constructor.newInstance(recordParameters.values().toArray()));
        } catch (ReflectiveOperationException | JsonParseException exception) {
            throw new RuntimeException(
                    String.format("Impossible to create a new instance of %s", clazz.getName()),
                    exception
            );
        }
    }

    /**
     * Десериализует объект класса.
     *
     * @param clazz         представление класса десериализуемого объекта в Java VM.
     * @param jsonStructure JSON-представление объекта.
     * @param exported      @Exported аннотация, содержащая метаинформацию о процессе десериализации.
     * @param <T>           тип десериализуемого объекта.
     * @return восстановленный объект типа T.
     */
    private <T> T restoreClassObject(Class<T> clazz, JsonStructure jsonStructure, Exported exported) {
        Constructor<?> constructor = getClassDefaultConstructor(clazz);

        List<? extends AnnotatedElement> exportedComponents = getExportedComponents(clazz);

        try {
            Object instance = constructor.newInstance();

            for (AnnotatedElement annotatedElement : exportedComponents) {
                String key;
                Class<?> genericType = null;

                Field field = (Field) annotatedElement;
                if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
                    genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }

                if (annotatedElement.isAnnotationPresent(PropertyName.class)) {
                    key = annotatedElement.getAnnotation(PropertyName.class).value().replaceAll("[\"]", "'");
                } else {
                    key = field.getName();
                }

                DateFormat dateFormat = null;
                if (annotatedElement.isAnnotationPresent(DateFormat.class)) {
                    dateFormat = annotatedElement.getAnnotation(DateFormat.class);
                }

                if (!jsonStructure.getStructureMap().containsKey(key)) {
                    if (exported.unknownPropertiesPolicy() == UnknownPropertiesPolicy.FAIL) {
                        throw new IllegalArgumentException();
                    } else {
                        continue;
                    }
                }

                Object value = restoreObject(
                        field.getType(),
                        jsonStructure.getStructureMap().get(key),
                        genericType,
                        dateFormat
                );
                field.set(instance, value);
            }

            return clazz.cast(instance);
        } catch (ReflectiveOperationException | JsonParseException exception) {
            throw new RuntimeException(
                    String.format("Impossible to create a new instance of %s", clazz.getName()),
                    exception
            );
        }
    }

    /**
     * Десериализует enum объект.
     *
     * @param clazz      представление класса десериализуемого объекта в Java VM.
     * @param jsonObject JSON-представление объекта.
     * @param <T>        тип десериализуемого объекта.
     * @return восстановленный объект типа T.
     */
    private <T> T restoreEnum(Class<T> clazz, JsonObject jsonObject) {
        try {
            Method enumValueOf = clazz.getMethod("valueOf", String.class);
            String value = jsonObject.value.toString().replaceAll(" ", "_").toUpperCase(Locale.ROOT);
            return clazz.cast(enumValueOf.invoke(null, value));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ищет доступный конструктор у record-класса десериализуемого объекта.
     *
     * @param clazz представление класса десериализуемого объекта в Java VM.
     * @return доступный конструктор.
     */
    private <T> T restoreTemporal(Class<T> clazz, JsonObject jsonObject, DateFormat dateFormat)
            throws JsonParseException {
        JsonTemporal jsonTemporal;
        if (dateFormat.value() != null) {
            jsonTemporal = new JsonTemporal(jsonObject.value.toString(), dateFormat.value());
        } else {
            jsonTemporal = new JsonTemporal(jsonObject.value.toString());
        }

        if (jsonTemporal.value instanceof LocalDate localDate) {
            return clazz.cast(localDate);
        } else if (jsonTemporal.value instanceof LocalTime localTime) {
            return clazz.cast(localTime);
        } else {
            return clazz.cast(jsonTemporal.value);
        }
    }

    /**
     * Ищет доступный конструктор без параметров у класса десериализуемого объекта.
     *
     * @param clazz представление класса десериализуемого объекта в Java VM.
     * @return доступный конструктор без параметров.
     */
    private Constructor<?> getClassDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> defaultConstructor = null;

        for (var constructor : constructors) {
            if (constructor.getGenericParameterTypes().length == 0 && constructor.getParameterCount() == 0) {
                defaultConstructor = constructor;
            }
        }

        if (defaultConstructor == null || !defaultConstructor.trySetAccessible()) {
            String exceptionMessage = String.format(
                    "Deserializable class %s must have an available constructor without parameters.",
                    clazz.getName()
            );
            throw new IllegalArgumentException(exceptionMessage);
        }

        return defaultConstructor;
    }

    private Constructor<?> getRecordConstructor(Class<?> clazz) {
        if (clazz.isRecord()) {
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];

            if (!constructor.trySetAccessible()) {
                String exceptionMessage = String.format(
                        "Deserializable record class %s must have an available constructor.",
                        clazz.getName()
                );
                throw new IllegalArgumentException(exceptionMessage);
            }

            return constructor;
        } else {
            throw new IllegalArgumentException("Class must be a Record.");
        }
    }

    /**
     * Получает список экспортируемых полей класса или компонент record-класса.
     *
     * @param clazz представление класса сериализуемого/десериализуемого объекта в Java VM.
     * @return список экспортируемых элементов, не помеченных аннотацией @Ignored.
     */
    private List<? extends AnnotatedElement> getExportedComponents(Class<?> clazz) {
        List<? extends AnnotatedElement> exportedComponents;

        if (clazz.isRecord()) {
            exportedComponents = Arrays.stream(clazz.getRecordComponents())
                    .filter(field -> !field.isAnnotationPresent(Ignored.class))
                    .toList();
        } else {
            List<Field> exportedFields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> !field.isAnnotationPresent(Ignored.class))
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .filter(field -> !field.isSynthetic()).toList();
            exportedFields.forEach(AccessibleObject::trySetAccessible);

            exportedComponents = exportedFields;
        }

        return exportedComponents;
    }
}
