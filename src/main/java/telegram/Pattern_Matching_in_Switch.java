package telegram;

public class Pattern_Matching_in_Switch {
    static void main() {
        Object obj = Math.random() > 0.5 ? 42 : "Hello";
        System.out.println(ifPatternMatching(obj));
        System.out.println(switchPatternMatching(obj));
        System.out.println(guardedPatterns(obj));
    }

    private static String ifPatternMatching(Object obj) {
        String result;
        // Instanceof is a runtime check. When I've tried to use it with
        // (Object) new Scanner(System.in).next() -> it was always String
        if (obj instanceof String s) {
            result = "String: " + s;
        } else if (obj instanceof Integer i) {
            result = "Integer: " + i;
        } else {
            result = "Unknown";
        }
        return result;
    }

    private static String switchPatternMatching(Object obj) {
        return switch (obj) {
            case String s -> "String: " + s;
            case Integer i -> "Integer: " + i;
            default -> "Unknown";
        };
    }

    /*
        Условия when проверяются последовательно.
        Компилятор отслеживает полноту покрытия и недостижимый код.
        Поменяете порядок кейсов неправильно — получите ошибку компиляции.
     */
    private static String guardedPatterns(Object obj) {
        return switch (obj) {
            case String s when s.length() > 10 -> "Long string: " + s;
            case String s -> "Short string: " + s;
            case Integer i when i > 0 -> "Positing: " + i;
            case Integer i -> "Non-positive: " + i;
            case null -> "Null";
            default -> "Unknown";
        };
    }

    record Point(int x, int y) {
    }

    /*
        Распаковали record прямо в case.
        Никаких геттеров, никаких промежуточных переменных.
     */
    String recordPatterns(Object obj) {
        return switch (obj) {
            case Point(int x, int y) when x == y -> "Diagonal point";
            case Point(int x, int y) -> "Point at (%d, %d)".formatted(x, y);
            default -> "Not a point!";
        };
    }

    sealed interface Result permits Result.Success, Result.Failure {
        record Success(String data) implements Result {
        }

        record Failure(String error) implements Result {
        }
    }

    /*
        Компилятор гарантирует, что вы обработали все случаи.
        Добавите новый класс в sealed иерархию — код не скомпилится,
            пока не обработаете его.
     */
    String sealedClassesWithPatternMatching(Result result) {
        return switch (result) {
            case Result.Success(String data) -> "Got: " + data;
            case Result.Failure(String error) -> "Error: " + error;
            // default не нужен - компилятор знает все варианты
        };
    }

    /*
        JIT оптимизирует pattern matching свитчи агрессивно.
        В бенчмарках разница с if-else цепочками от 2x до 10x
            в пользу switch в зависимости от количества веток.
     */
}
