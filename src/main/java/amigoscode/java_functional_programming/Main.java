package amigoscode.java_functional_programming;

import java.util.ArrayList;
import java.util.List;

import static amigoscode.java_functional_programming.Main.Gender.FEMALE;
import static amigoscode.java_functional_programming.Main.Gender.MALE;

public class Main {
    static void main() {
        List<Person> people = List.of(
                new Person("Pawa", MALE),
                new Person("Miha", MALE),
                new Person("Ola", FEMALE)
        );

        List<Person> females = new ArrayList<Person>();

        for (Person person : people) {
            if (person.gender.equals(FEMALE)) {
                females.add(person);
            }
        }

        for (Person female : females) {
            System.out.println(female);
        }

    }

    static class Person {

        private String name;
        private Gender gender;

        public Person(String name, Gender gender) {
            this.name = name;
            this.gender = gender;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", gender=" + gender +
                    '}';
        }

    }

    enum Gender {MALE, FEMALE}

}
