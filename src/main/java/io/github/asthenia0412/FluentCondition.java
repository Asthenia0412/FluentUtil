package io.github.asthenia0412;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.Objects;

/**
 * 线程安全的流式条件判断工具类
 * <p>
 * 采用不可变对象设计模式确保线程安全，所有修改操作都会返回新实例
 *
 * @param <T> 封装值的类型
 */
public final class FluentCondition<T> {
    private static final Logger logger = LoggerFactory.getLogger(FluentCondition.class);

    // 不可变状态
    // private final T value
    // private final boolean shortCircuit
    // private final String context;
    private final T value;
    private final boolean shortCircuit;
    private final String context; // 改为final字段，不再使用ThreadLocal

    /**
     * 私有构造方法
     * @param value 封装的值
     * @param shortCircuit 是否短路标志
     * @param context 上下文信息
     */
    private FluentCondition(T value, boolean shortCircuit, String context) {
        this.value = value;
        this.shortCircuit = shortCircuit;
        this.context = context;
    }

    /**
     * 静态工厂方法，创建新实例
     * @param value 要封装的值
     * @param <T> 值的类型
     * @return 新的FluentCondition实例
     */
    public static <T> FluentCondition<T> of(T value) {
        return new FluentCondition<>(value, false, null);
    }

    /**
     * 设置上下文信息
     * @param context 上下文描述
     * @return 新的FluentCondition实例（包含新上下文）
     */
    public FluentCondition<T> withContext(String context) {
        logger.debug("Set context: {}", context);
        return new FluentCondition<>(this.value, this.shortCircuit, context);
    }

    /**
     * 条件判断并执行操作
     * @param predicate 条件谓词
     * @param action 满足条件时执行的操作
     * @return 新的FluentCondition实例
     */
    public FluentCondition<T> when(Predicate<T> predicate, Consumer<T> action) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        Objects.requireNonNull(action, "Action cannot be null");

        if (!shortCircuit && predicate.test(value)) {
            logger.debug("Condition met for value: {}, context: {}", value, context);
            action.accept(value);
        }
        return this; // 返回当前实例，因为操作不改变状态
    }

    /**
     * 条件判断并抛出异常
     * @param predicate 条件谓词
     * @param exceptionSupplier 异常提供者
     * @return 如果条件不满足返回当前实例，否则抛出异常
     * @throws RuntimeException 当条件满足时抛出
     */
    public FluentCondition<T> when(Predicate<T> predicate, Supplier<RuntimeException> exceptionSupplier) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        Objects.requireNonNull(exceptionSupplier, "Exception supplier cannot be null");

        if (!shortCircuit && predicate.test(value)) {
            RuntimeException ex = exceptionSupplier.get();
            logger.error("Exception triggered for value: {}, context: {}, thread: {}, method: {}",
                    value, context, Thread.currentThread().getName(), getCallerMethod(), ex);
            throw ex;
        }
        return this;
    }

    /**
     * Else分支处理
     * @param action 要执行的操作
     * @return 新的FluentCondition实例
     */
    public FluentCondition<T> orElse(Consumer<T> action) {
        Objects.requireNonNull(action, "Action cannot be null");

        if (!shortCircuit) {
            logger.debug("Executing orElse for value: {}, context: {}", value, context);
            action.accept(value);
        }
        return this;
    }

    /**
     * 非空校验
     * @param errorMessage 错误信息
     * @return 如果非空返回当前实例，否则抛出异常
     * @throws IllegalArgumentException 当值为null时抛出
     */
    public FluentCondition<T> notNull(String errorMessage) {
        return when(v -> v == null, () -> new IllegalArgumentException(errorMessage));
    }

    /**
     * 非空字符串校验
     * @param errorMessage 错误信息
     * @return 如果是非空字符串返回当前实例，否则抛出异常
     * @throws IllegalArgumentException 当值为空字符串时抛出
     */
    public FluentCondition<T> notEmptyString(String errorMessage) {
        return when(v -> v instanceof String && ((String) v).isEmpty(),
                () -> new IllegalArgumentException(errorMessage));
    }

    /**
     * 正数校验
     * @param errorMessage 错误信息
     * @return 如果是正数返回当前实例，否则抛出异常
     * @throws IllegalArgumentException 当值不是正数时抛出
     */
    public FluentCondition<T> positiveNumber(String errorMessage) {
        return when(v -> v instanceof Number && ((Number) v).doubleValue() <= 0,
                () -> new IllegalArgumentException(errorMessage));
    }

    /**
     * 自定义条件校验
     * @param predicate 自定义条件
     * @param errorMessage 错误信息
     * @return 如果条件不满足返回当前实例，否则抛出异常
     * @throws IllegalArgumentException 当条件满足时抛出
     */
    public FluentCondition<T> check(Predicate<T> predicate, String errorMessage) {
        return when(predicate, () -> new IllegalArgumentException(errorMessage));
    }

    /**
     * 链式执行操作
     * @param action 要执行的操作
     * @return 新的FluentCondition实例
     */
    public FluentCondition<T> then(Consumer<T> action) {
        Objects.requireNonNull(action, "Action cannot be null");

        if (!shortCircuit) {
            logger.debug("Executing then for value: {}, context: {}", value, context);
            action.accept(value);
        }
        return this;
    }

    /**
     * 默认值处理
     * @param defaultSupplier 默认值提供者
     * @return 如果当前值为null返回包含默认值的新实例，否则返回当前实例
     */
    public FluentCondition<T> orElseGet(Supplier<T> defaultSupplier) {
        Objects.requireNonNull(defaultSupplier, "Default supplier cannot be null");

        if (!shortCircuit && value == null) {
            T newValue = defaultSupplier.get();
            logger.debug("Using default value: {}, context: {}", newValue, context);
            return new FluentCondition<>(newValue, false, context);
        }
        return this;
    }

    /**
     * 类型转换
     * @param mapper 类型转换函数
     * @param <R> 目标类型
     * @return 包含转换后值的新实例，如果已短路则返回空值实例
     */
    public <R> FluentCondition<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");

        if (!shortCircuit) {
            R newValue = mapper.apply(value);
            logger.debug("Mapped value from {} to {}, context: {}", value, newValue, context);
            return new FluentCondition<>(newValue, false, context);
        }
        return new FluentCondition<>(null, true, context);
    }

    /**
     * 终止条件链
     * @return 新的已短路实例
     */
    public FluentCondition<T> end() {
        logger.debug("Chain terminated for value: {}, context: {}", value, context);
        return new FluentCondition<>(value, true, context);
    }

    /**
     * 获取封装的值
     * @return 封装的值
     */
    public T get() {
        return value;
    }

    /**
     * 获取调用方法信息（用于日志）
     * @return 调用方法全名
     */
    private String getCallerMethod() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            return stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName();
        }
        return "Unknown";
    }
}