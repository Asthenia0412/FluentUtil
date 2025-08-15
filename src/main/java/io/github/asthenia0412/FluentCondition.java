package io.github.asthenia0412;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.Function;

public class FluentCondition<T> {
    private static final Logger logger = LoggerFactory.getLogger(FluentCondition.class);
    private final T value;
    private volatile boolean shortCircuit = false; // 使用volatile确保多线程可见性
    private final ThreadLocal<String> context = new ThreadLocal<>(); // 存储上下文信息

    private FluentCondition(T value) {
        this.value = value;
    }

    // 1. 静态工厂方法
    public static <T> FluentCondition<T> of(T value) {
        return new FluentCondition<>(value);
    }

    // 2. 设置上下文信息，便于调试
    public FluentCondition<T> withContext(String context) {
        this.context.set(context);
        logger.debug("Set context: {}", context);
        return this;
    }

    // 3. 条件判断，执行操作
    public FluentCondition<T> when(Predicate<T> predicate, Consumer<T> action) {
        if (!shortCircuit && predicate.test(value)) {
            logger.debug("Condition met for value: {}, context: {}", value, context.get());
            action.accept(value);
        }
        return this;
    }

    // 4. 条件判断，抛出异常
    public FluentCondition<T> when(Predicate<T> predicate, Supplier<RuntimeException> exceptionSupplier) {
        if (!shortCircuit && predicate.test(value)) {
            shortCircuit = true;
            RuntimeException ex = exceptionSupplier.get();
            logger.error("Exception triggered for value: {}, context: {}, thread: {}, method: {}",
                    value, context.get(), Thread.currentThread().getName(), getCallerMethod(), ex);
            throw ex;
        }
        return this;
    }

    // 5. Else分支
    public FluentCondition<T> orElse(Consumer<T> action) {
        if (!shortCircuit) {
            logger.debug("Executing orElse for value: {}, context: {}", value, context.get());
            action.accept(value);
        }
        return this;
    }

    // 6. 判空校验
    public FluentCondition<T> notNull(String errorMessage) {
        return when(v -> v == null, () -> new IllegalArgumentException(errorMessage));
    }

    // 7. 非空字符串校验
    public FluentCondition<T> notEmptyString(String errorMessage) {
        return when(v -> v instanceof String && ((String) v).isEmpty(),
                () -> new IllegalArgumentException(errorMessage));
    }

    // 8. 正数校验
    public FluentCondition<T> positiveNumber(String errorMessage) {
        return when(v -> v instanceof Number && ((Number) v).doubleValue() <= 0,
                () -> new IllegalArgumentException(errorMessage));
    }

    // 9. 自定义条件校验
    public FluentCondition<T> check(Predicate<T> predicate, String errorMessage) {
        return when(predicate, () -> new IllegalArgumentException(errorMessage));
    }

    // 10. 链式执行Consumer
    public FluentCondition<T> then(Consumer<T> action) {
        if (!shortCircuit) {
            logger.debug("Executing then for value: {}, context: {}", value, context.get());
            action.accept(value);
        }
        return this;
    }

    // 11. 默认值处理
    public FluentCondition<T> orElseGet(Supplier<T> defaultSupplier) {
        if (!shortCircuit && value == null) {
            T newValue = defaultSupplier.get();
            logger.debug("Using default value: {}, context: {}", newValue, context.get());
            return new FluentCondition<>(newValue);
        }
        return this;
    }

    // 12. 类型转换
    public <R> FluentCondition<R> map(Function<T, R> mapper) {
        if (!shortCircuit) {
            R newValue = mapper.apply(value);
            logger.debug("Mapped value from {} to {}, context: {}", value, newValue, context.get());
            return new FluentCondition<>(newValue);
        }
        return new FluentCondition<>(null);
    }

    // 13. 条件终止
    public FluentCondition<T> end() {
        shortCircuit = true;
        logger.debug("Chain terminated for value: {}, context: {}", value, context.get());
        return this;
    }

    // 14. 获取结果
    public T get() {
        return value;
    }

    // 获取调用方法信息，用于异常日志
    private String getCallerMethod() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            return stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName();
        }
        return "Unknown";
    }
}