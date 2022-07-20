package testingclasses;

import ru.hse.homework4.*;

import java.time.LocalDateTime;
import java.util.List;

@Exported(nullHandling = NullHandling.INCLUDE)
public class Person {
    public enum Sex {
        MALE,
        FEMALE
    }

    private String name;

    @Ignored
    String surname;
    private int age;

    public Sex sex;

    @PropertyName("date of registration")
    @DateFormat("uuuu-MMMM-dd HH:mm:ss")
    private LocalDateTime registrationTimestamp;

    @PropertyName("is married")
    boolean isMarried;

    public List<Person> children;

    char group;

    public Person(String name, String surname, int age, Sex sex, LocalDateTime registrationTimestamp,
                  boolean isMarried, List<Person> children, char group) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.sex = sex;
        this.registrationTimestamp = registrationTimestamp;
        this.isMarried = isMarried;
        this.children = children;
        this.group = group;
    }

    public Person() {
    }
}
