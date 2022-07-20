package ru.whiteroomlz.mapper;

import org.junit.jupiter.api.*;
import testingclasses.Person;
import testingclasses.Point2D;
import testingclasses.Point3D;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonMapperTests {
    private final int WRITE_TEST_ORDER = 0;
    private final int READ_TEST_ORDER = 1;

    private final Person person;
    private final Point2D point2D;

    JsonMapperTests() {
        Person son = new Person("John", "Smith", 18, Person.Sex.MALE,
                null, false, List.of(), '-');
        Person daughter = new Person("Joan", "Smith", 15, Person.Sex.FEMALE,
                LocalDateTime.parse("2020-02-05T00:00:00.0"), false, null, '\n');
        person = new Person("Paul", "Smith", 37, Person.Sex.MALE,
                LocalDateTime.parse("2022-02-26T02:32:52.0"), true, List.of(son, daughter), 'C');

        point2D = new Point2D(1.0, 2.4);
    }

    @Test
    @Order(WRITE_TEST_ORDER)
    void classWriteToString_ReturnStringEqualsExpected() {
        JsonMapper mapper = new JsonMapper(false);
        String serialized = mapper.writeToString(person);

        Path documentPath = Path.of("src", "test", "resources", "paul.json");
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(documentPath))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            Assertions.assertEquals(jsonDocument, serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(WRITE_TEST_ORDER)
    void classWrite_WriteFileWithTextEqualsExpected() {
        JsonMapper mapper = new JsonMapper(false);
        Path serializationPath = Path.of("src", "test", "resources", "paul_test.json");

        Person deserialized;
        try {
            mapper.write(person, new File(String.valueOf(serializationPath)));
            deserialized = mapper.read(Person.class, new File(String.valueOf(serializationPath)));
        } catch (IOException e) {
            deserialized = null;
            e.printStackTrace();
        }

        String serialized = mapper.writeToString(deserialized);

        Path documentPath = Path.of("src", "test", "resources", "paul.json");
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(documentPath))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            Assertions.assertEquals(jsonDocument, serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(WRITE_TEST_ORDER)
    void recordWrite_WriteFileByOutputStreamWithTextEqualsExpected() {
        JsonMapper mapper = new JsonMapper(false);
        Path serializationPath = Path.of("src", "test", "resources", "point_test.json");

        Point2D deserialized;
        try {
            mapper.write(point2D, new FileOutputStream(String.valueOf(serializationPath)));
            deserialized = mapper.read(Point2D.class, new FileInputStream(String.valueOf(serializationPath)));
        } catch (IOException e) {
            deserialized = null;
            e.printStackTrace();
        }

        String serialized = mapper.writeToString(deserialized);

        Path documentPath = Path.of("src", "test", "resources", "point.json");
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(documentPath))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            Assertions.assertEquals(jsonDocument, serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(WRITE_TEST_ORDER)
    void recordWriteToString_ReturnStringEqualsExpected() {
        JsonMapper mapper = new JsonMapper(false);
        String serialized = mapper.writeToString(point2D);

        Path documentPath = Path.of("src", "test", "resources", "point.json");
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(documentPath))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            Assertions.assertEquals(jsonDocument, serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(READ_TEST_ORDER)
    void classRead_ReturnExpectedClassOrRecordObject() throws IOException {
        JsonMapper mapper = new JsonMapper(false);
        Path documentPath = Path.of("src", "test", "resources", "paul.json");
        Person person = mapper.read(Person.class, new File(documentPath.toString()));
        String serialized = mapper.writeToString(person);

        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(documentPath))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            Assertions.assertEquals(jsonDocument, serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(READ_TEST_ORDER)
    void recordRead_ReturnExpectedClassOrRecordObject() throws IOException {
        JsonMapper mapper = new JsonMapper(false);
        Path documentPath = Path.of("src", "test", "resources", "point.json");
        Point2D point2D = mapper.read(Point2D.class, new File(documentPath.toString()));
        String serialized = mapper.writeToString(point2D);

        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(documentPath))) {
            String jsonDocument = reader.lines().collect(Collectors.joining());
            Assertions.assertEquals(jsonDocument, serialized);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(READ_TEST_ORDER)
    void retainIdentityTrue_ShouldReturnSameObjectsIfTheyWereSame() {
        JsonMapper mapper = new JsonMapper(true);

        Person person1 = this.person;
        Person person2 = this.person;
        Person person = new Person("Maul", "Smith", 58, Person.Sex.FEMALE,
                LocalDateTime.parse("2022-02-26T02:32:52.0"), true, List.of(person1, person2), 'D');
        String str = mapper.writeToString(person);

        Person restored = mapper.readFromString(Person.class, str);

        Assertions.assertSame(restored.children.get(0), restored.children.get(1));
    }

    @Test
    @Order(READ_TEST_ORDER)
    void retainIdentityTrue_ShouldReturnNotSameObjectsIfTheyWereNotSame() {
        JsonMapper mapper = new JsonMapper(true);

        Person person1 = new Person("John", "Smith", 18, Person.Sex.MALE,
                null, false, List.of(), '-');
        Person person2 = new Person("John", "Smith", 18, Person.Sex.MALE,
                null, false, List.of(), '-');
        Person person = new Person("Maul", "Smith", 58, Person.Sex.FEMALE,
                LocalDateTime.parse("2022-02-26T02:32:52.0"), true, List.of(person1, person2), 'D');
        String str = mapper.writeToString(person);

        Person restored = mapper.readFromString(Person.class, str);

        Assertions.assertNotSame(restored.children.get(0), restored.children.get(1));
    }

    @Test
    @Order(READ_TEST_ORDER)
    void retainIdentityFalse_ShouldReturnNotSameObjectsIfTheyWereSame() {
        JsonMapper mapper = new JsonMapper(false);

        Person person1 = this.person;
        Person person2 = this.person;
        Person person = new Person("Maul", "Smith", 58, Person.Sex.FEMALE,
                LocalDateTime.parse("2022-02-26T02:32:52.0"), true, List.of(person1, person2), 'D');
        String str = mapper.writeToString(person);

        Person restored = mapper.readFromString(Person.class, str);

        Assertions.assertNotSame(restored.children.get(0), restored.children.get(1));
    }

    @Test
    @Order(READ_TEST_ORDER)
    void unknownPropertiesPolicyFail_ShouldThrowIllegalArgumentException() {
        JsonMapper mapper = new JsonMapper(false);
        Path documentPath = Path.of("src", "test", "resources", "point.json");

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> mapper.read(Point3D.class, new File(documentPath.toString()))
        );
    }
}
