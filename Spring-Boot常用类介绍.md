# Spring Boot 常用类介绍

> 以本项目的实际代码为例，介绍 Spring Boot 最核心的类和注解。

---

## 1. 入口类 `@SpringBootApplication`

**项目示例：** [LibraryApplication.java](backend-java/src/main/java/com/library/LibraryApplication.java)

```java
@SpringBootApplication
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
```

`@SpringBootApplication` 是一个组合注解，等价于三个注解打包：

| 注解 | 作用 |
|------|------|
| `@Configuration` | 标记该类是配置类，可在其中定义 `@Bean` |
| `@EnableAutoConfiguration` | Spring Boot 的核心——根据 classpath 自动配置。例如检测到 H2 就自动配 DataSource，检测到 `spring-boot-starter-web` 就自动配 Tomcat + DispatcherServlet |
| `@ComponentScan` | 从该类所在包开始递归扫描 `@Component`、`@Service`、`@Controller` 等并注册为 Bean |

`SpringApplication.run()` 做了三步：**创建 ApplicationContext → 自动配置 → 启动嵌入式 Tomcat**。整个过程无需任何 XML 配置。

---

## 2. `@RestController` + `@RequestMapping`

**项目示例：** [AuthController.java](backend-java/src/main/java/com/library/controller/AuthController.java)

```java
@RestController                     // = @Controller + @ResponseBody
@RequestMapping("/api/auth")        // 类级别路径前缀
public class AuthController {

    @PostMapping("/login")          // 相当于 @RequestMapping(method = POST)
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        ...
    }
}
```

### 请求处理流程

```
用户请求 → DispatcherServlet（前端控制器）
        → HandlerMapping（找到匹配的 Controller 方法）
        → HttpMessageConverter（Jackson 把 JSON 转成 LoginRequest 对象）
        → login() 方法执行
        → HttpMessageConverter（把 Result 对象转回 JSON）
        → 返回响应
```

### 常用派生注解

| 注解 | 等价写法 |
|------|---------|
| `@GetMapping("/path")` | `@RequestMapping(value = "/path", method = GET)` |
| `@PostMapping("/path")` | `@RequestMapping(value = "/path", method = POST)` |
| `@PutMapping("/path")` | `@RequestMapping(value = "/path", method = PUT)` |
| `@DeleteMapping("/path")` | `@RequestMapping(value = "/path", method = DELETE)` |

---

## 3. 参数绑定注解

你的 Controller 里用到了五种参数注入方式：

| 注解 | 项目示例 | 说明 |
|------|---------|------|
| `@PathVariable` | `@PathVariable Long id` | 从 URL 路径中取值：`/api/books/5` → `id=5` |
| `@RequestParam` | `@RequestParam(defaultValue = "1") int page` | 从查询参数取值：`?page=2&pageSize=10` |
| `@RequestBody` | `@RequestBody Book book` | 把请求体 JSON 反序列化为 Java 对象（通过 Jackson） |
| `@AuthenticationPrincipal` | 注入 `UserPrincipal` | Spring Security 专属，直接获取当前登录用户信息 |
| `@Valid` | 配合 `@RequestBody` | 触发 Jakarta Validation 校验（`@NotBlank`、`@NotNull`、`@Size` 等） |

### 示例：参数注解的组合使用

项目里的 [BorrowController.java](backend-java/src/main/java/com/library/controller/BorrowController.java)：

```java
@PostMapping
public Result<?> borrow(
        @AuthenticationPrincipal UserPrincipal user,   // 当前登录用户（从 JWT 解析而来）
        @Valid @RequestBody BorrowRequest request      // JSON 请求体 + 参数校验
) {
    BorrowRecord record = borrowService.borrow(user.getId(), request.getBookId());
    return Result.success(record);
}
```

而 [BookController.java](backend-java/src/main/java/com/library/controller/BookController.java) 的 `update` 用了 `@PathVariable` + `@RequestBody`：

```java
@PutMapping("/{id}")
public Result<Book> update(@PathVariable Long id, @RequestBody Book book) {
    ...
}
```

---

## 4. `@Service` + `@Transactional`

**项目示例：** [BorrowServiceImpl.java](backend-java/src/main/java/com/library/service/impl/BorrowServiceImpl.java)

```java
@Service                              // 标记为业务层 Bean
public class BorrowServiceImpl implements BorrowService {

    @Value("${library.max-borrow-count}")   // 从 application.yml 注入配置
    private int maxBorrowCount;

    @Override
    @Transactional                    // 开启事务
    public BorrowRecord borrow(Long userId, Long bookId) {
        // ① 锁定用户行：同一用户的并发借阅在此串行化
        User user = userMapper.selectByIdForUpdate(userId);
        // ② 检查借阅上限
        Long borrowedCount = borrowRecordMapper.selectCount(...);
        if (borrowedCount != null && borrowedCount >= maxBorrowCount)
            throw new RuntimeException("借阅已达上限");
        // ③ 原子扣减库存：条件 UPDATE，防并发超卖
        int affected = bookMapper.decrementAvailableCopies(bookId);
        if (affected == 0) throw new RuntimeException("无可用副本");
        // ④ 创建借阅记录（含应还日期）
        borrowRecordMapper.insert(record);
        // 任何一步抛异常，前面的操作自动回滚
        return record;
    }
}
```

### 关键点

- `@Service` 本质跟 `@Component` 一样，只是语义上区分层级（Controller → Service → Mapper）
- `@Transactional` 通过 **AOP 代理** 实现：方法执行前开启事务 → 正常返回则提交 → 抛出 RuntimeException 则回滚
- **注意：** 只有通过代理对象调用的方法事务才生效。同类内部直接调用（`this.xxx()`）不走代理，事务不会生效
- `@Value("${key}")` 把 `application.yml` 里的配置注入字段，项目用它控制借阅期限（`borrow-duration-days`）和借阅上限（`max-borrow-count`）

---

## 5. `@Configuration` + `@Bean`

**项目示例：** [SecurityConfig.java](backend-java/src/main/java/com/library/config/SecurityConfig.java)

```java
@Configuration                        // 告诉 Spring 这是一个配置类
@EnableWebSecurity                    // 开启 Spring Security 自动配置
public class SecurityConfig {

    @Bean                             // 方法返回值注册为 Spring Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())              // 前后端分离 + JWT，无需 CSRF
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                .requestMatchers("/api/books/**").hasRole("admin")
                .requestMatchers("/api/users/**").hasRole("admin")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

当前 `SecurityConfig` 一共注册了 **4 个 `@Bean`**：`filterChain`、`passwordEncoder`、`authenticationManager`、`corsConfigurationSource`。

### `@Bean` vs `@Component` 的区别

| 方式 | 使用场景 | 示例 |
|------|---------|------|
| `@Component` 标注类 | 你有源码的类，让 Spring 自动扫描并实例化 | `@Service`、`@RestController` |
| `@Bean` 标注方法 | 第三方库的类（你改不了源码），或者创建过程需要复杂逻辑 | `BCryptPasswordEncoder`、`SecurityFilterChain` |

---

## 6. 生命周期接口

**项目示例：** [DataInitializer.java](backend-java/src/main/java/com/library/config/DataInitializer.java)

```java
@Component
public class DataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // 所有 Bean 初始化完毕后自动执行
        initAdmin();
        initBooks();
        initTestUsersAndBorrowRecords();
    }
}
```

### 常用生命周期接口对比

| 接口/注解 | 触发时机 | 适用场景 |
|----------|---------|---------|
| `CommandLineRunner` | 所有 Bean 初始化完毕后执行，可拿到原始命令行参数 | 数据初始化、缓存预热 |
| `ApplicationRunner` | 同上，但参数被封装为 `ApplicationArguments` | 需要解析命令行选项时 |
| `@PostConstruct` | Bean 构造 + 依赖注入完毕后执行（比 Runner 早得多） | 单 Bean 初始化逻辑 |
| `@EventListener(ApplicationReadyEvent.class)` | 应用完全就绪后触发（最晚） | 需要确认所有基础设施就绪的操作 |

---

## 7. `@ControllerAdvice` 全局异常处理

**项目示例：** [GlobalExceptionHandler.java](backend-java/src/main/java/com/library/common/GlobalExceptionHandler.java)

```java
@RestControllerAdvice            // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        // 所有 @Valid 校验失败都统一来这里处理
        String msg = e.getBindingResult().getFieldErrors()
            .stream().map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.error(400, msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        // 兜底：捕获所有未处理的运行时异常
        return Result.error(400, e.getMessage());
    }
}
```

### 异常处理优先级

```
1. Controller 内的 try-catch — 最先拦截
2. @ExceptionHandler(具体异常类型) — 精确匹配
3. @ExceptionHandler(父类异常类型) — 兜底匹配
```

你的项目里 Controller 和 GlobalExceptionHandler 各处理一部分异常，形成了 **双层异常防护**。

---

## 8. 过滤器 `OncePerRequestFilter`

**项目示例：** [JwtAuthenticationFilter.java](backend-java/src/main/java/com/library/security/JwtAuthenticationFilter.java)

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        String token = getTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 解析 JWT → 构建认证对象 → 存入 SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);  // 必须调用，否则请求中断
    }
}
```

### 为什么要继承 `OncePerRequestFilter`？

保证每个请求**只经过一次**。如果直接实现 `Filter` 接口，当一个请求发生内部 forward 时会被重复执行。`OncePerRequestFilter` 通过请求属性标记确保只过滤一次。

### 请求的完整过滤链

```
请求 → CORS Filter → CSRF Filter → JwtAuthenticationFilter
     → UsernamePasswordAuthenticationFilter → DispatcherServlet → Controller
```

你的 SecurityConfig 把 `JwtAuthenticationFilter` 插入在 `UsernamePasswordAuthenticationFilter` 之前：

```java
http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

---

## 9. MyBatis-Plus 基础类

除了 Spring Boot 标准类，项目还用了 MyBatis-Plus 的两个核心组件：

### `BaseMapper<T>`

你的 [UserMapper.java](backend-java/src/main/java/com/library/mapper/UserMapper.java)、[BookMapper.java](backend-java/src/main/java/com/library/mapper/BookMapper.java)、[BorrowRecordMapper.java](backend-java/src/main/java/com/library/mapper/BorrowRecordMapper.java) 都继承了它：

```java
@Mapper
public interface BookMapper extends BaseMapper<Book> {
    // 自动继承 BaseMapper 的 CRUD 方法
    // 另外还自定义了带并发控制语义的 SQL（见下面的 @Update）
}
```

继承后自动获得的方法（无需任何 XML 或 SQL）：

| 方法 | 功能 |
|------|------|
| `insert(T entity)` | 插入一条记录 |
| `deleteById(Serializable id)` | 按主键删除 |
| `updateById(T entity)` | 按主键更新 |
| `selectById(Serializable id)` | 按主键查询 |
| `selectList(Wrapper<T> wrapper)` | 条件查询多条 |
| `selectOne(Wrapper<T> wrapper)` | 条件查询一条 |
| `selectCount(Wrapper<T> wrapper)` | 条件统计数量 |
| `selectPage(Page<T> page, Wrapper<T> wrapper)` | 分页查询 |

### `LambdaQueryWrapper<T>`

类型安全的查询条件构造器，避免硬编码字段名：

```java
// 不用字符串 "username"，用 Java 方法引用，重构友好
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, "admin")         // WHERE username = 'admin'
       .gt(User::getCreatedAt, someDate)       // AND created_at > someDate
       .orderByDesc(User::getCreatedAt);       // ORDER BY created_at DESC
userMapper.selectList(wrapper);
```

### 自定义 SQL：`@Select` / `@Update` / `@Param`

`BaseMapper` 的方法不够用时，可以直接在 Mapper 接口方法上写注解 SQL，无需 XML：

```java
// UserMapper.java —— 悲观行锁，借书时串行化同一用户的并发请求
@Select("SELECT * FROM users WHERE id = #{id} FOR UPDATE")
User selectByIdForUpdate(@Param("id") Long id);

// BookMapper.java —— 条件 UPDATE，原子扣减库存，防止并发超卖
@Update("UPDATE books SET available_copies = available_copies - 1 " +
        "WHERE id = #{bookId} AND available_copies > 0")
int decrementAvailableCopies(@Param("bookId") Long bookId);
```

| 注解 | 作用 |
|------|------|
| `@Select` | 查询 SQL，返回值映射为对象 / 列表 / 分页对象 |
| `@Update` | 更新 SQL，返回值为受影响行数 |
| `@Param("name")` | 给方法参数起名，SQL 中用 `#{name}` 引用（多参数时必须显式指定） |

这套组合是项目并发安全的核心：**用户行锁**保证借阅上限检查准确，**条件 UPDATE** 保证库存不会被扣成负数。

---

## 总结：项目中的 Spring Bean 全景图

```
LibraryApplication (@SpringBootApplication)     ← 启动入口
    │
    ├── config/
    │   ├── SecurityConfig        @Configuration + 4个@Bean + @EnableWebSecurity
    │   ├── MyBatisPlusConfig     @Configuration + 1个@Bean + 实现 MetaObjectHandler
    │   └── DataInitializer       @Component + CommandLineRunner
    │
    ├── security/
    │   ├── JwtTokenProvider      @Component（JWT 生成/解析工具）
    │   ├── JwtAuthenticationFilter  @Component（请求过滤器，继承 OncePerRequestFilter）
    │   └── UserPrincipal         POJO（实现 UserDetails，每次请求动态创建）
    │
    ├── controller/               ← 接收 HTTP 请求
    │   ├── AuthController        @RestController + @RequestMapping("/api/auth")
    │   ├── BookController        @RestController + @RequestMapping("/api/books")
    │   ├── BorrowController      @RestController + @RequestMapping("/api/borrow")
    │   ├── UserController        @RestController + @RequestMapping("/api/users")
    │   ├── StatsController       @RestController + @RequestMapping("/api/stats")
    │   └── HomeController        @RestController（GET /）
    │
    ├── service/                  ← 接口定义
    │   └── 4个 *Service          纯接口，无注解
    │
    ├── service/impl/             ← 业务逻辑实现
    │   ├── AuthServiceImpl       @Service
    │   ├── BookServiceImpl       @Service
    │   ├── BorrowServiceImpl     @Service（含 @Transactional）
    │   └── UserServiceImpl       @Service
    │
    ├── mapper/                   ← 数据访问
    │   └── 3个 *Mapper           @Mapper + extends BaseMapper（自动生成代理）
    │                             └── 自定义 @Select / @Update（行锁、原子扣减库存）
    │
    ├── dto/                      ← 请求/响应数据传输对象
    │   ├── LoginRequest/LoginResponse、RegisterRequest
    │   └── BorrowRequest、ReturnRequest（配合 @Valid 校验）
    │
    └── common/                   ← 通用组件
        ├── Result                POJO（统一响应封装）
        ├── PageResult            POJO（分页结果封装）
        └── GlobalExceptionHandler  @RestControllerAdvice
```

---

## 核心注解速查表

| 注解 | 作用 | 所在层 |
|------|------|-------|
| `@SpringBootApplication` | 启动入口，组合了配置+自动配置+组件扫描 | 根目录 |
| `@RestController` | REST 控制器，返回值自动转 JSON | Controller |
| `@RequestMapping("/path")` | 定义 URL 路径映射 | Controller |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | 指定 HTTP 方法的路径映射 | Controller |
| `@PathVariable` | 绑定 URL 路径参数 | Controller 方法参数 |
| `@RequestParam` | 绑定查询参数 | Controller 方法参数 |
| `@RequestBody` | 绑定 JSON 请求体 | Controller 方法参数 |
| `@Valid` | 触发 Jakarta Validation 校验 | Controller 方法参数 |
| `@AuthenticationPrincipal` | 注入当前登录用户（`UserPrincipal`） | Controller 方法参数 |
| `@Service` | 标记业务层 Bean | Service |
| `@Transactional` | 声明事务（AOP 代理实现） | Service 方法 |
| `@Configuration` | 标记配置类 | Config |
| `@Bean` | 声明方法返回值为 Spring Bean | Config 方法 |
| `@EnableWebSecurity` | 启用 Spring Security Web 支持 | Config |
| `@Component` | 通用 Bean 注册 | 任意 |
| `@Mapper` | MyBatis-Plus 数据访问接口 | Mapper |
| `@Select` / `@Update` | 在 Mapper 方法上直接写 SQL | Mapper 方法 |
| `@Param("name")` | 为 SQL 中的 `#{name}` 绑定参数 | Mapper 方法参数 |
| `@RestControllerAdvice` | 全局异常处理 + JSON 响应 | Common |
| `@ExceptionHandler` | 指定捕获的异常类型 | Common 方法 |
| `@Value("${key}")` | 读取 application.yml / properties 配置值 | 任意 |
| `@PostConstruct` | Bean 初始化后回调 | 任意方法 |
