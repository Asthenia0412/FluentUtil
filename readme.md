# 🌟 FluentCondition

**FluentCondition** 是一个基于 Java 8+ 的**流畅式（Fluent API）条件判断工具类**，提供链式调用能力，用于优雅地处理 **条件判断、参数校验、异常抛出、默认值、类型转换等逻辑**。

它尤其适用于以下场景：

- 参数校验（非空、非空字符串、正数等）
- 条件分支处理（满足条件执行操作 / 抛出异常）
- 链式逻辑编排（then / else / map 等）
- 调试与上下文信息记录
- 工具类、中间件、框架等底层代码的条件控制

------

## 📦 项目信息

| 属性            | 值                            |
| --------------- | ----------------------------- |
| **Group ID**    | `io.github.asthenia0412`      |
| **Artifact ID** | `FluentUtil`                  |
| **当前版本**    | `0.0.1-SNAPSHOT`              |
| **包名**        | `io.github.asthenia0412`      |
| **主要类**      | `FluentCondition<T>`          |
| **语言**        | Java 8+                       |
| **依赖**        | SLF4J（日志门面，可选但推荐） |

------

## 🚀 功能特性

`FluentCondition<T>`提供如下能力：

### ✅ 基础功能

- **链式调用（Fluent API）**：支持流畅的 `.when().then().orElse()`风格调用
- **泛型支持**：支持任意类型 `T`的条件判断与处理
- **线程安全设计**：使用 `volatile`和 `ThreadLocal`保证并发安全与上下文隔离

### ✅ 条件判断 & 动作执行

- `.when(Predicate<T>, Consumer<T>)`：当条件成立时，执行某个操作
- `.when(Predicate<T>, Supplier<RuntimeException>)`：当条件成立时，抛出指定异常
- `.orElse(Consumer<T>)`：条件不满足时执行备用逻辑
- `.then(Consumer<T>)`：无论是否命中前面的条件，只要未中断，都可以执行后续逻辑

### ✅ 常用校验（快捷方法）

- `.notNull(String)`：校验值不为 null，否则抛出异常
- `.notEmptyString(String)`：校验字符串非空（非 `""`），否则抛异常
- `.positiveNumber(String)`：校验数字大于 0，否则抛异常
- `.check(Predicate<T>, String)`：自定义条件，不满足时抛异常

### ✅ 高级功能

- `.map(Function<T, R>)`：对值进行类型转换，返回一个新的 FluentCondition<R>
- `.orElseGet(Supplier<T>)`：如果当前值为 null，则使用默认值
- `.withContext(String)`：设置上下文信息，便于日志与调试
- `.end()`：终止后续所有条件判断
- `.get()`：获取当前包装的值

------

## 📌 使用示例

### 1. 基础用法：条件判断 + 执行操作

```java
FluentCondition.of("Hello")
    .when(s -> s.equals("Hello"), s -> System.out.println("条件匹配，值为: " + s))
    .orElse(s -> System.out.println("不匹配，执行默认逻辑"));
```

### 2. 参数校验：非空 & 非空字符串 & 正数

```java
String input = null;

FluentCondition.of(input)
    .notNull("输入不能为空")                // 校验非 null
    .notEmptyString("输入不能是空字符串")   // 校验非 ""
    .positiveNumber("必须为正数");          // 如果是数字，校验 > 0
```

> ⚠️ 注意：`positiveNumber()`会对任意 `Number`类型（如 Integer、Double）判断是否 ≤ 0

### 3. 自定义条件 + 异常抛出

```java
FluentCondition.of(42)
    .check(value -> value.equals(0), "值不能为 0")
    .orElse(() -> System.out.println("值合法，继续执行"));
```

### 4. 类型转换 & 链式操作

```java
FluentCondition.of("123")
    .map(Integer::parseInt)         // 转成 Integer
    .when(i -> i > 100, i -> System.out.println("大于100的数字: " + i))
    .then(i -> System.out.println("最终数字是: " + i));
```

### 5. 使用上下文 & 安全终止

```java
FluentCondition.of(someValue)
    .withContext("用户输入校验阶段")
    .notNull("值不能为空")
    .end() // 终止后续判断
    .get(); // 获取值
```

------

## 🛠️ 依赖

本项目基于 [SLF4J](https://www.slf4j.org/)作为日志门面，**推荐你在使用方项目中引入具体的日志实现（如 Logback、Log4j）**，以查看内部调试日志。

Maven 依赖（仅 FluentCondition 类本身无第三方依赖，但内部使用了 SLF4J）：

```
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.36</version> <!-- 或你项目中的版本 -->
</dependency>
```

------

## 📦 打包 & 引入

### 构建 JAR 包

使用 Maven 打包：

```bash
mvn clean package
```

生成的 JAR 文件位于：

```
target/FluentUtil-0.0.1-SNAPSHOT.jar
```

> ⚠️ 当前为普通 Java 类库 JAR，**不包含依赖（如 slf4j-api）**，使用者需自行引入所需依赖。

------

### 如何引入到你的项目

如果你将此项目发布到了 **Maven 私服 / GitHub Packages / 公开仓库**，其他项目可以这样引入：

```xml
<dependency>
    <groupId>io.github.asthenia0412</groupId>
    <artifactId>FluentUtil</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

🔧 如果你尚未发布，可以：

- 直接以 **源码依赖方式** 引入（复制 `FluentCondition.java`到你的项目中）
- 或使用本地 Maven 安装：

```
mvn clean install
```

然后其他本地项目可通过：

```xml
<dependency>
    <groupId>io.github.asthenia0412</groupId>
    <artifactId>FluentUtil</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

引用它。

------

## 🧪 单元测试

本项目包含完整的单元测试（基于 JUnit 5 + AssertJ），覆盖所有主要 API，包括：

- 条件判断
- 异常抛出
- 默认值处理
- 类型转换
- 链式调用逻辑

测试代码位于：`src/test/java/io/github/asthenia0412/FluentConditionTest.java`

运行测试：

```bash
mvn test
```

------

## 🤝 贡献 & 开发

欢迎提交 Issue 或 Pull Request，你可以帮助扩展如下功能：

- 更多内置校验规则（如邮箱、URL、日期格式等）
- 集合/数组校验支持
- 支持多语言错误消息
- 增强日志与监控
- 支持 Kotlin 扩展

## ✨ 特性总结（TL;DR）

| 特性                 | 支持 |
| -------------------- | ---- |
| 流畅 API（链式调用） | ✅    |
| 泛型支持             | ✅    |
| 条件判断 / 异常抛出  | ✅    |
| 默认值 / Else 分支   | ✅    |
| 类型转换（map）      | ✅    |
| 日志与上下文         | ✅    |
| 线程安全             | ✅    |
| 单元测试覆盖         | ✅    |
| 易于集成 & 扩展      | ✅    |

