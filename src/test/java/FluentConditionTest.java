
import io.github.asthenia0412.FluentCondition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

class FluentConditionTest {

    @Test
    void testOf() {
        FluentCondition<String> condition = FluentCondition.of("Hello");
        Assertions.assertThat(condition).isNotNull();
    }

    @Test
    void testWithContext() {
        FluentCondition<String> condition = FluentCondition.of("test")
                .withContext("demo-context");
        // Context is internal (ThreadLocal), we can't directly assert, but method should not fail
        Assertions.assertThat(condition).isNotNull();
    }

    @Test
    void testWhenPredicateConsumer() {
        StringBuilder log = new StringBuilder();
        Consumer<String> consumer = s -> log.append(s);

        FluentCondition.of("trigger")
                .when(s -> s.equals("trigger"), consumer);

        Assertions.assertThat(log.toString()).isEqualTo("trigger");
    }

    @Test
    void testWhenPredicateException() {
        Assertions.assertThatThrownBy(() ->
                        FluentCondition.of("bad")
                                .when(s -> s.equals("bad"), () -> new IllegalArgumentException("Bad value"))
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bad value");
    }

    @Test
    void testOrElse() {
        StringBuilder log = new StringBuilder();
        Consumer<String> consumer = s -> log.append(s);

        FluentCondition.of("value")
                .orElse(consumer);

        Assertions.assertThat(log.toString()).isEqualTo("value");
    }

    @Test
    void testNotNull() {
        Assertions.assertThatThrownBy(() ->
                        FluentCondition.of(null)
                                .notNull("Value must not be null")
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must not be null");
    }

    @Test
    void testNotNullWithNonNullValue() {
        // Should not throw
        FluentCondition.of("not-null")
                .notNull("Should not throw");
    }

    @Test
    void testNotEmptyString() {
        Assertions.assertThatThrownBy(() ->
                        FluentCondition.of("")
                                .notEmptyString("String must not be empty")
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("String must not be empty");
    }

    @Test
    void testNotEmptyStringWithNonEmpty() {
        // Should not throw
        FluentCondition.of("hello")
                .notEmptyString("Should not throw");
    }

    @Test
    void testPositiveNumber() {
        Assertions.assertThatThrownBy(() ->
                        FluentCondition.of(0)
                                .positiveNumber("Number must be positive")
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Number must be positive");
    }

    @Test
    void testPositiveNumberWithPositive() {
        // Should not throw
        FluentCondition.of(1)
                .positiveNumber("Should not throw");
    }

    @Test
    void testCheck() {
        Assertions.assertThatThrownBy(() ->
                        FluentCondition.of("fail")
                                .check(s -> s.equals("fail"), "Custom check failed")
                ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Custom check failed");
    }

    @Test
    void testThen() {
        StringBuilder log = new StringBuilder();
        Consumer<String> consumer = s -> log.append(s);

        FluentCondition.of("then")
                .then(consumer);

        Assertions.assertThat(log.toString()).isEqualTo("then");
    }

    @Test
    void testOrElseGet() {
        FluentCondition<Object> condition = FluentCondition.of(null)
                .orElseGet(() -> "default");

        Assertions.assertThat(condition.get()).isEqualTo("default");
    }

    @Test
    void testOrElseGetWithNonNull() {
        FluentCondition<String> condition = FluentCondition.of("real")
                .orElseGet(() -> "default");

        Assertions.assertThat(condition.get()).isEqualTo("real");
    }

    @Test
    void testMap() {
        FluentCondition<Integer> mapped = FluentCondition.of("123")
                .map(s -> s.length());

        Assertions.assertThat(mapped.get()).isEqualTo(3);
    }

    @Test
    void testEnd() {
        // Just ensure it doesn't throw and can be chained
        FluentCondition.of("test")
                .when(s -> true, s -> {})
                .end()
                .get();
    }

    @Test
    void testGet() {
        String value = FluentCondition.of("actual").get();
        Assertions.assertThat(value).isEqualTo("actual");
    }
}