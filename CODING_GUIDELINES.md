# 编码指引 (Coding Guidelines)

## 1. 项目概述

### 1.1 技术栈
- **框架**: Spring Boot 2.5.5
- **JDK**: 1.8
- **构建工具**: Maven
- **测试框架**: JUnit + Spring Boot Test

### 1.2 项目结构
```
giencoder-learn/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/    # 主代码目录
│   │   └── resources/                 # 配置文件
│   └── test/
│       └── java/com/example/demo/    # 测试代码目录
├── pom.xml                            # Maven 配置
├── Dockerfile                         # Docker 构建
└── .gitlab-ci.yml                     # CI/CD 配置
```

---

## 2. 代码规范

### 2.1 命名规范

#### 类命名
- 使用 **PascalCase**（大驼峰）
- 类名应为名词，清晰表达职责
- 示例：
  ```java
  public class OrderController { }
  public class UserService { }
  public class ProductRepository { }
  ```

#### 方法命名
- 使用 **camelCase**（小驼峰）
- 方法名应为动词 + 名词
- 布尔方法使用 `is/has/can/should` 前缀
- 示例：
  ```java
  public User getUserById(Long id) { }
  public boolean isValid() { }
  public void createOrder(Order order) { }
  ```

#### 变量命名
- 使用 **camelCase**
- 有意义的名称，避免单字母（循环变量除外）
- 常量使用 **UPPER_SNAKE_CASE**
- 示例：
  ```java
  private String userName;
  private static final int MAX_RETRY_COUNT = 3;
  ```

#### 包命名
- 全部小写，使用点号分隔
- 格式：`com.company.project.module`
- 示例：
  ```java
  package com.example.demo.controller;
  package com.example.demo.service;
  package com.example.demo.repository;
  ```

### 2.2 代码格式

#### 缩进与空格
- 使用 4 个空格缩进（不使用 Tab）
- 运算符两侧添加空格
- 方法调用括号前无空格

#### 大括号
- 使用 K&R 风格（行尾大括号）
- 所有控制结构必须使用大括号

```java
// 正确
if (condition) {
    doSomething();
}

// 错误
if (condition)
    doSomething();
```

#### 行长度
- 单行不超过 120 字符
- 超长字符串使用 `+` 换行

### 2.3 注释规范

#### 类注释
```java
/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 * @author your-name
 * @since 2026-07-01
 */
public class UserService { }
```

#### 方法注释
```java
/**
 * 根据 ID 获取用户
 * @param id 用户 ID
 * @return 用户对象，不存在时返回 null
 * @throws IllegalArgumentException 当 ID 为空时
 */
public User getUserById(Long id) { }
```

#### 行内注释
- 使用 `//`，后跟一个空格
- 解释 **为什么** 而非 **是什么**

```java
// 重试 3 次以防止网络抖动
for (int i = 0; i < MAX_RETRY_COUNT; i++) { }
```

---

## 3. 架构规范

### 3.1 分层架构

```
Controller 层 → Service 层 → Repository 层
     ↓              ↓              ↓
   DTO           Entity          DB
```

#### Controller 层
- 仅处理 HTTP 请求/响应
- 参数校验
- 调用 Service 层
- 不包含业务逻辑

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
```

#### Service 层
- 包含核心业务逻辑
- 事务管理
- 调用 Repository 层

```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return convertToDTO(user);
    }
}
```

#### Repository 层
- 数据访问接口
- 继承 JpaRepository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
}
```

### 3.2 异常处理

#### 自定义异常
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

#### 全局异常处理器
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }
}
```

### 3.3 响应格式

```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    // 构造函数、getter/setter
}
```

---

## 4. 测试规范

### 4.1 测试分类

#### 单元测试
- 测试单个类/方法
- 使用 Mock 隔离依赖
- 文件后缀：`*Test.java`

```java
@SpringBootTest
public class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @Test
    public void testGetUserById() {
        // given
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // when
        UserDTO result = userService.getUserById(1L);
        
        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
```

#### 集成测试
- 测试多个组件协作
- 使用真实数据库（H2/测试库）
- 文件后缀：`*IntegrationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

### 4.2 测试命名
- 使用 `test` 前缀或 `should` 描述
- 清晰表达测试场景

```java
@Test
public void testGetUserById_WhenUserExists_ReturnsUser() { }

@Test
public void shouldThrowException_WhenUserIdIsNull() { }
```

### 4.3 测试覆盖率
- 目标：行覆盖率 ≥ 80%
- 核心业务：行覆盖率 ≥ 90%
- 使用 JaCoCo 生成报告

---

## 5. Git 规范

### 5.1 分支管理

```
main          - 主分支，生产环境
develop       - 开发分支
feature/*     - 功能分支（如：feature/picking-function）
bugfix/*      - 修复分支
release/*     - 发布分支
hotfix/*      - 紧急修复分支
```

### 5.2 Commit 信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 类型
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具

#### 示例
```
feat(picking): 添加拣货单创建功能

- 实现拣货单实体类
- 添加拣货单 Repository
- 创建拣货单 API 接口

Closes #123
```

### 5.3 代码审查
- 所有代码必须经过 Code Review
- MR 至少需要 1 人审批
- CI 检查必须通过

---

## 6. 安全规范

### 6.1 输入校验
- 所有外部输入必须校验
- 使用 Hibernate Validator

```java
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String userName;
    
    @Email
    private String email;
}
```

### 6.2 SQL 注入防护
- 使用参数化查询
- 禁止字符串拼接 SQL

```java
// 正确
@Query("SELECT u FROM User u WHERE u.userName = :userName")
User findByUserName(@Param("userName") String userName);

// 错误 - 禁止使用
@Query("SELECT u FROM User u WHERE u.userName = '" + userName + "'")
```

### 6.3 敏感信息
- 密码必须加密存储（BCrypt）
- API 密钥存放在环境变量
- 日志中不输出敏感信息

---

## 7. 性能规范

### 7.1 数据库
- 添加必要的索引
- 避免 N+1 查询
- 使用分页查询大数据量

```java
@Query("SELECT u FROM User u")
Page<User> findAllUsers(Pageable pageable);
```

### 7.2 缓存
- 热点数据使用缓存
- 设置合理的过期时间

```java
@Cacheable(value = "users", key = "#id", ttl = 3600)
public User getUserById(Long id) { }
```

### 7.3 异步处理
- 耗时操作使用异步
- 使用 @Async 注解

```java
@Async
public CompletableFuture<Void> sendEmailAsync(String email) { }
```

---

## 8. 开发流程

### 8.1 本地开发
```bash
# 1. 克隆项目
git clone <repository-url>
cd giencoder-learn

# 2. 安装依赖
mvn clean install

# 3. 运行项目
mvn spring-boot:run

# 4. 运行测试
mvn test
```

### 8.2 代码提交流程
1. 从 `develop` 创建功能分支
2. 开发并编写测试
3. 本地运行测试通过
4. 提交代码（遵循 Commit 规范）
5. 推送分支并创建 Merge Request
6. Code Review
7. CI 检查通过
8. 合并到 `develop`

### 8.3 Docker 部署
```bash
# 构建镜像
docker build -t giencoder-learn:latest .

# 运行容器
docker run -p 8080:8080 giencoder-learn:latest
```

---

## 9. 常用命令

### Maven 命令
```bash
mvn clean              # 清理
mvn compile            # 编译
mvn test               # 测试
mvn package            # 打包
mvn spring-boot:run    # 运行
mvn clean install      # 清理并安装
```

### Git 命令
```bash
git checkout -b feature/xxx    # 创建功能分支
git add .                      # 添加文件
git commit -m "feat: xxx"      # 提交
git push origin feature/xxx    # 推送
```

---

## 10. 参考资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/Alibaba-Java-Coding-Guidelines)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Clean Code](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)

---

*最后更新：2026-07-01*
