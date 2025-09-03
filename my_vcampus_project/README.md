# VCampus 虚拟校园管理系统

## 🎓 项目简介

VCampus是一个基于Java的虚拟校园管理系统，采用客户端-服务器架构，实现了完整的用户认证、角色管理和界面交互功能。系统支持学生端和教职工端的不同功能模块，提供了现代化的用户界面和安全的密码管理功能。

## 🏗️ 项目架构

```
my_vcampus_project/
├── common/                    # 公共模块 - 共享的基础工具类和接口定义
├── server/                   # 服务端模块 - 核心业务逻辑和数据处理
│   └── core/                 # 核心业务逻辑
│       ├── auth/             # 认证模块
│       ├── library/          # 图书管理模块
│       ├── student/          # 学生管理模块
│       ├── admin/            # 管理员模块
│       ├── academic/         # 教务管理模块
│       ├── finance/          # 财务管理模块
│       └── net/              # 网络通信层
├── client/                   # 客户端模块 - 用户界面和网络通信
│   └── core/                 # 客户端核心
│       ├── net/              # 网络通信
│       ├── service/          # 客户端服务层
│       └── ui/               # 用户界面层（模块化架构）
├── config/                   # 配置文件目录
├── database/                 # 数据库脚本
└── docs/                     # 文档目录
```

### 🎯 模块化UI架构

采用**模块导向架构**，按业务功能组织界面代码：

```
client/core/ui/
├── auth/               # 🔐 认证模块
├── student/            # 👨‍🎓 学生功能模块
├── admin/              # 👨‍💼 管理员模块
├── library/            # 📚 图书管理模块
├── academic/           # 🎓 教务管理模块
├── finance/            # 💰 财务管理模块
├── component/          # 🔧 公共UI组件
└── common/             # 📦 通用界面组件
```

### 技术栈

- **后端**: Java 21 (LTS) + Netty + MyBatis
- **前端**: Java Swing + FlatLAF + 模块化UI架构
- **数据库**: MySQL + MyBatis ORM框架
- **构建工具**: Gradle 8.0+
- **通信协议**: JSON over TCP
- **安全性**: 密码加密、会话管理、权限控制
- **架构模式**: 客户端-服务器架构 + 模块化设计

### UI架构特色

- **模块化设计**: 按业务功能而非用户角色组织界面
- **组件化开发**: 可复用的自定义UI组件
- **统一样式**: FlatLAF现代化外观 + 自定义主题
- **响应式布局**: 自适应不同屏幕尺寸
- **用户体验**: 动画效果、快捷键支持、状态反馈

## 🚀 快速开始

### 环境要求

- **JDK 21 (LTS)** - 必需，支持最新的语言特性和性能优化
- **MySQL 8.0+** - 数据库服务器
- **Gradle 8.0+** - 必需，支持JDK21的现代构建系统
- **内存**: 建议4GB+ RAM
- **操作系统**: Windows 10+, macOS 10.15+, Linux

> 📖 **详细配置说明**: 请查看 [docs/JDK21_CONFIGURATION.md](docs/JDK21_CONFIGURATION.md) 了解完整的JDK21配置要求和最佳实践。

### 1. 数据库配置

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE vcampus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 导入数据库结构（推荐方式）
mysql -u root -p vcampus < database/init_new.sql
mysql -u root -p vcampus < database/library_data.sql
mysql -u root -p vcampus < database/library_functions.sql

# 或者导入完整初始化脚本（兼容方式）
mysql -u root -p vcampus < database/init.sql

# 3. 配置数据库连接
# 编辑 config/database.properties 文件
```

### 2. 编译项目

```bash
# 编译所有模块
./gradlew build

# 或者在Windows上
gradlew.bat build
```

### 3. 启动服务器

```bash
# 启动服务器（默认端口8080）
./gradlew :server:run

# 或指定端口
./gradlew :server:run --args="9090"
```

服务器启动后会显示：

```
🚀 VCampus服务器启动成功，监听端口: 8080
📱 客户端可以连接到: localhost:8080
📊 数据库连接已建立
🔐 用户认证系统已就绪
```

### 4. 启动客户端

```bash
# 启动客户端
```

## 📡 API接口文档

### 系统接口

- `system/heartbeat` - 心跳检测
- `system/info` - 服务器信息
- `system/time` - 服务器时间
- `system/echo` - 回显测试

### 认证接口

- `auth/login` - 用户登录
- `auth/logout` - 用户登出 [需要登录]
- `auth/userinfo` - 获取用户信息 [需要登录]
- `auth/changepassword` - 修改密码 [需要登录]

### 用户管理接口

- `user/list` - 获取用户列表 [管理员权限]
- `user/info` - 获取用户详细信息 [需要登录]
- `user/update` - 更新用户信息 [需要登录]

### 学生管理接口

- `student/info` - 获取学生个人信息 [学生权限]
- `student/update` - 更新学生信息 [学生权限]
- `student/list` - 获取学生列表 [教师权限]

## 🎮 服务器控制台命令

启动服务器后，可以在控制台使用以下命令：

- `help` - 显示帮助信息
- `status` - 显示服务器状态
- `routes` - 显示所有注册的路由
- `stats` - 显示服务器统计信息
- `users` - 显示在线用户
- `reset` - 重置统计信息
- `stop` - 停止服务器

## 📝 消息格式

### 请求格式 (Request)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "uri": "auth/login",
  "params": {
    "username": "2021001",
    "password": "student123"
  },
  "session": null,
  "timestamp": 1699123456789
}
```

### 响应格式 (Response)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "message": "登录成功",
  "data": {
    "cardNum": 2021001,
    "userName": "张三",
    "primaryRole": "student",
    "roles": ["student"],
    "gender": "男",
    "phone": "13800138000"
  },
  "timestamp": 1699123456790
}
```

## 🔧 开发指南

> 📖 查看完整的[模块开发指南](docs/MODULE-DEVELOPMENT-GUIDE.md)，了解如何开发新模块。

### MyBatis数据访问架构

项目已全面迁移到MyBatis框架，提供更强大的数据访问能力：

#### 1. MyBatis配置结构

```
server/src/main/resources/
├── mybatis-config.xml          # MyBatis主配置文件
└── mapper/                     # SQL映射文件目录
    ├── UserMapper.xml          # 用户数据映射
    ├── BookMapper.xml          # 图书数据映射
    ├── BookBorrowMapper.xml    # 借阅记录映射
    ├── BookCategoryMapper.xml  # 图书分类映射
    └── BorrowRuleMapper.xml    # 借阅规则映射
```

#### 2. Mapper接口定义

```java
// 示例：BookMapper.java
@Mapper
public interface BookMapper {
    Book findById(@Param("bookId") Integer bookId);
    List<Book> findByTitleLike(@Param("title") String title);
    List<Book> findAvailableBooks();
    int insert(Book book);
    int update(Book book);
    int deleteById(@Param("bookId") Integer bookId);
}
```

#### 3. XML映射文件

```xml
<!-- 示例：BookMapper.xml -->
<mapper namespace="com.vcampus.server.core.library.mapper.BookMapper">
    <resultMap id="BookResultMap" type="com.vcampus.server.core.library.entity.Book">
        <id column="book_id" property="bookId"/>
        <result column="title" property="title"/>
        <result column="author" property="author"/>
        <!-- 更多字段映射... -->
    </resultMap>
  
    <select id="findById" parameterType="int" resultMap="BookResultMap">
        SELECT * FROM t_book WHERE book_id = #{bookId}
    </select>
</mapper>
```

#### 4. DAO层实现

```java
// 示例：BookDao.java
public class BookDao {
    private final SqlSessionFactory sqlSessionFactory;
  
    public Optional<Book> findById(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findById(bookId);
            return Optional.ofNullable(book);
        }
    }
}
```

#### 5. MyBatis优势

- **SQL与Java分离**: 便于SQL优化和维护
- **动态SQL支持**: 灵活的条件查询
- **自动结果映射**: 减少手动映射代码
- **缓存机制**: 内置一级和二级缓存
- **类型安全**: 编译时检查SQL参数
- **插件扩展**: 支持分页、性能监控等插件

### 添加新的控制器

1. 在 `server/src/main/java/com/vcampus/server/core/{模块}/controller/` 创建新的控制器类
2. 使用 `@RouteMapping` 注解标记方法：

```java
@RouteMapping(uri = "example/hello", role = "student", description = "示例接口")
public Response hello(Request request) {
    return Response.Builder.success("Hello World!");
}
```

### 自定义客户端界面

我们的项目采用**模块化UI架构**，按业务功能组织界面代码：

#### 1. 创建新的业务模块界面

```bash
# 在对应的业务模块目录下创建界面类
client/src/main/java/com/vcampus/client/core/ui/{模块名}/
├── {模块名}MainPanel.java     # 模块主面板
├── {功能}Panel.java           # 功能面板
├── {功能}Dialog.java          # 对话框
└── components/                # 模块专用组件
```

#### 2. 界面类设计规范

```java
// 主面板类 - 继承JPanel，作为模块入口
public class LibraryMainPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;
  
    public LibraryMainPanel() {
        initComponents();
        setupLayout();
        setupListeners();
    }
}

// 功能面板类 - 实现具体功能
public class BookSearchPanel extends JPanel {
    private final LibraryClientService service = LibraryClientService.getInstance();
  
    // 使用客户端服务进行数据交互
}

// 对话框类 - 用于弹窗操作
public class BookDetailDialog extends JDialog {
    // 实现图书详情显示和编辑
}
```

#### 3. 集成到主界面

```java
// 在StudentFrame中添加模块入口
JButton libraryButton = new JButton("📚 图书馆");
libraryButton.addActionListener(e -> {
    setContentPanel(new LibraryMainPanel());
});

// 在AdminFrame中添加管理入口  
JButton libraryAdminButton = new JButton("📖 图书管理");
libraryAdminButton.addActionListener(e -> {
    setContentPanel(new LibraryAdminPanel());
});
```

#### 4. UI组件使用规范

- 使用 `client/core/ui/component/` 下的通用组件
- 遵循FlatLAF设计规范
- 实现响应式布局和用户体验优化

## 📚 模块开发指南

### 开发流程

1. **数据库设计** - 在 `database/` 目录定义表结构
2. **实体类开发** - 在对应模块的 `entity/` 目录创建实体类
3. **MyBatis映射** - 创建Mapper接口和XML映射文件
4. **DAO层开发** - 实现数据访问对象
5. **服务层开发** - 实现业务逻辑
6. **控制器开发** - 创建API接口
7. **客户端开发** - 实现用户界面

### 目录结构

```
server/core/{模块名}/
├── controller/     # 控制器层
├── service/        # 服务层
├── dao/           # 数据访问层
├── mapper/        # MyBatis映射器
├── entity/        # 实体类
├── enums/         # 业务枚举
└── constant/      # 业务常量
```




## 🚀 部署说明

### 开发环境
1. 配置JDK 21环境
2. 启动MySQL数据库
3. 运行服务器端：`./gradlew :server:run`
4. 运行客户端：`./gradlew :client:run`

### 生产环境
1. 构建发布包：`./gradlew build`
2. 部署服务器jar包
3. 配置数据库连接
4. 分发客户端应用程序

## 🔑 模块开发最佳实践

### 分层设计原则

#### 1. Common模块（共享层）

**目的**: 提供项目通用的基础设施和工具类，不包含具体业务实体

**组织原则**:

- **entity/base/**: 放置基础实体接口和抽象类（如IEntity接口）
- **dao/**: 数据访问基础接口和抽象类
- **util/**: 工具类，按功能分类组织
- **message/**: 网络通信消息格式
- **enums/**: 通用枚举定义（非业务特定）

**命名规范**:

```java
// 基础接口：I+功能名
public interface IEntity { }
public interface IDao<T> { }

// 工具类：功能+Utils后缀
public class DateUtils { }
public class JsonUtils { }

// 通用枚举：通用概念
public enum Gender { MALE, FEMALE }
public enum Status { ACTIVE, INACTIVE }
```

#### 2. Server模块（业务层）

**目的**: 实现核心业务逻辑和数据处理，包含所有业务实体类

**Core架构**:

```
server/core/
├── {业务模块}/          # 按业务领域划分
│   ├── controller/     # 接口控制层
│   ├── service/        # 业务逻辑层
│   ├── dao/           # 数据访问层
│   ├── entity/        # 业务实体类（NEW!）
│   ├── constant/      # 业务常量类（NEW!）
│   └── util/          # 模块专用工具
├── common/            # 服务器通用组件
│   ├── annotation/    # 自定义注解
│   ├── router/        # 路由处理
│   └── exception/     # 异常定义
├── net/              # 网络通信层
└── db/               # 数据库基础设施
```

**重要架构原则**:

> 🔄 **实体类组织原则（NEW!）**: 业务实体类（如Book、BookBorrow）现在存放在各自的业务模块中（server/core/{module}/entity/），而不是common模块。这样可以：
>
> - 提高模块内聚性，业务实体与业务逻辑紧密结合
> - 降低模块间耦合，避免common模块过度膨胀
> - 便于模块独立开发和维护
> - 支持微服务架构的后续演进

**业务模块示例**:

- `auth/`: 认证授权模块（User, Role等实体）
- `student/`: 学生管理模块（Student等实体）
- `library/`: 图书管理模块（Book, BookBorrow等实体）
- `academic/`: 学术事务模块（Course, Grade等实体）
- `finance/`: 财务管理模块（Payment, Bill等实体）

#### 3. Client模块（表示层）

**目的**: 提供用户界面和客户端网络通信

**Core架构**:

```
client/core/
├── net/              # 网络通信层
├── service/          # 客户端服务层
├── ui/               # 用户界面层
│   ├── component/    # 自定义UI组件
│   └── {业务模块}/    # 按业务分组的界面
└── {业务模块}/        # 业务模块客户端代码
    ├── service/      # 模块客户端服务
    └── ui/           # 模块界面
```

### 接口设计规范

#### 1. URI命名规范

```java
// 格式：{业务模块}/{功能}/{操作}
"auth/login"              // 用户登录
"auth/logout"             // 用户注销
"library/search"          // 图书搜索
"library/borrow"          // 图书借阅
"library/admin/add"       // 管理员添加图书
"student/info"            // 获取学生信息
"student/update"          // 更新学生信息
```

#### 2. 权限控制规范

```java
@RouteMapping(uri = "library/search", role = "*", description = "图书搜索")
// role = "*" : 所有用户可访问

@RouteMapping(uri = "library/borrow", role = "student,teacher", description = "借阅图书")
// role = "student,teacher" : 学生和教师可访问

@RouteMapping(uri = "library/admin/add", role = "admin", description = "添加图书")
// role = "admin" : 仅管理员可访问
```

#### 3. 参数传递规范

```java
// 简单参数
Map<String, Object> params = Map.of("bookId", 1L, "userId", 100L);

// 复杂对象使用JSON序列化
Book book = new Book();
// ... 设置属性
Map<String, Object> params = Map.of("book", JsonUtils.toJson(book));
```

#### 4. 响应格式规范

```java
// 成功响应
return Response.Builder.success(data);

// 错误响应
return Response.Builder.error("错误信息");

// 带消息的成功响应
return Response.Builder.success(data, "操作成功");
```

### 代码组织规范

#### 1. 包命名规范

```java
// 基础包结构
com.vcampus.common.*                        // 公共模块（基础设施）
com.vcampus.server.core.*                   // 服务器核心
com.vcampus.client.core.*                   // 客户端核心

// 业务模块包结构
com.vcampus.server.core.library.controller.*   // 图书馆控制器
com.vcampus.server.core.library.service.*      // 图书馆服务
com.vcampus.server.core.library.dao.*          // 图书馆DAO
com.vcampus.server.core.library.entity.*       // 图书馆实体类（NEW!）
com.vcampus.server.core.library.constant.*     // 图书馆常量（NEW!）
com.vcampus.client.core.library.ui.*           // 图书馆UI
```

#### 2. 类命名规范

```java
// 控制器类：业务+Controller
public class LibraryController { }
public class AuthController { }

// 服务类：业务+Service/+ServiceImpl
public interface LibraryService { }
public class LibraryServiceImpl implements LibraryService { }

// DAO类：实体+Dao/+DaoImpl
public interface BookDao extends IDao<Book> { }
public class BookDaoImpl extends AbstractDao<Book> implements BookDao { }

// UI类：功能+Frame/Panel/Dialog
public class LoginFrame extends JFrame { }
public class BookSearchPanel extends JPanel { }
public class BookDetailDialog extends JDialog { }
```

#### 3. 方法命名规范

```java
// 查询方法：find/get/search
public List<Book> findByTitle(String title) { }
public Book getBookById(Long id) { }
public List<Book> searchBooks(Map<String, Object> conditions) { }

// 操作方法：动词+宾语
public BookBorrow borrowBook(Long userId, Long bookId) { }
public BookBorrow returnBook(Long borrowId) { }
public Book addBook(Book book) { }
public Book updateBook(Book book) { }
public void deleteBook(Long id) { }

// 事件处理方法：handle+事件
private void handleLogin() { }
private void handleSearch() { }
private void handleSave() { }
```

### 数据访问模式

#### 1. MyBatis DAO模式实现

```java
// 1. 定义Mapper接口
public interface BookMapper {
    Book findById(@Param("bookId") Integer bookId);
    List<Book> findByTitleLike(@Param("title") String title);
    List<Book> findByAuthorLike(@Param("author") String author);
    Book findByIsbn(@Param("isbn") String isbn);
    List<Book> findAll();
    int insert(Book book);
    int update(Book book);
    int deleteById(@Param("bookId") Integer bookId);
}

// 2. 实现DAO类
public class BookDao {
    private final SqlSessionFactory sqlSessionFactory;
  
    public Optional<Book> findById(Integer bookId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findById(bookId);
            return Optional.ofNullable(book);
        }
    }
  
    public List<Book> findByTitleLike(String title) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            return mapper.findByTitleLike(title);
        }
    }
}

// 3. 在Service中使用
public class LibraryServiceImpl implements LibraryService {
    private final BookDao bookDao = BookDao.getInstance();
  
    @Override
    public List<Book> searchBooks(Map<String, Object> conditions) {
        String title = (String) conditions.get("title");
        if (title != null && !title.trim().isEmpty()) {
            return bookDao.findByTitleLike(title);
        }
        return bookDao.findAll();
    }
}
```

#### 2. 动态SQL查询

```xml
<!-- 在BookMapper.xml中定义动态SQL -->
<select id="searchBooks" parameterType="map" resultMap="BookResultMap">
    SELECT * FROM t_book
    <where>
        <if test="title != null and title != ''">
            AND title LIKE CONCAT('%', #{title}, '%')
        </if>
        <if test="author != null and author != ''">
            AND author LIKE CONCAT('%', #{author}, '%')
        </if>
        <if test="category != null and category != ''">
            AND category = #{category}
        </if>
        <if test="availableOnly == true">
            AND available_count > 0
        </if>
    </where>
    <if test="orderBy != null">
        ORDER BY ${orderBy}
    </if>
    <if test="limit != null">
        LIMIT #{limit}
    </if>
</select>
```

#### 3. 复杂查询示例

```java
// 在Mapper接口中定义
List<Book> searchBooksWithConditions(@Param("title") String title,
                                   @Param("author") String author,
                                   @Param("category") String category,
                                   @Param("availableOnly") Boolean availableOnly,
                                   @Param("orderBy") String orderBy,
                                   @Param("limit") Integer limit);

// 在DAO中使用
public List<Book> searchBooks(Map<String, Object> conditions) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
        BookMapper mapper = session.getMapper(BookMapper.class);
        return mapper.searchBooksWithConditions(
            (String) conditions.get("title"),
            (String) conditions.get("author"),
            (String) conditions.get("category"),
            (Boolean) conditions.get("availableOnly"),
            (String) conditions.get("orderBy"),
            (Integer) conditions.get("limit")
        );
    }
}
```

### 用户界面设计

#### 1. 界面层次结构

```java
// 主框架类
public class StudentFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
  
    // 切换内容面板
    public void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}

// 功能面板类
public class LibraryMainPanel extends JPanel {
    // 实现具体功能界面
}
```

#### 2. 事件处理模式

```java
public class BookSearchPanel extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JTable resultTable;
  
    private void setupListeners() {
        // 搜索按钮事件
        searchButton.addActionListener(e -> handleSearch());
      
        // 回车键搜索
        searchField.addActionListener(e -> handleSearch());
      
        // 表格双击查看详情
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleViewDetail();
                }
            }
        });
    }
  
    private void handleSearch() {
        // 实现搜索逻辑
    }
  
    private void handleViewDetail() {
        // 实现查看详情逻辑
    }
}
```

### 异常处理策略

#### 1. Service层异常处理

```java
public class LibraryServiceImpl implements LibraryService {
    @Override
    public BookBorrow borrowBook(Long userId, Long bookId) throws Exception {
        // 业务逻辑校验
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("图书不存在");
        }
        if (book.getAvailableCount() <= 0) {
            throw new IllegalStateException("图书库存不足");
        }
      
        // 执行业务操作
        try {
            // ... 借阅逻辑
            return borrowDao.save(borrow);
        } catch (Exception e) {
            log.error("借阅图书失败: userId={}, bookId={}", userId, bookId, e);
            throw new RuntimeException("借阅操作失败", e);
        }
    }
}
```

#### 2. Controller层异常处理

```java
public class LibraryController {
    @RouteMapping(uri = "library/borrow", role = "student,teacher", description = "借阅图书")
    public Response borrowBook(Request request) {
        try {
            Long userId = request.getSession().getUserId();
            Long bookId = Long.valueOf(request.getParams().get("bookId").toString());
          
            BookBorrow borrow = libraryService.borrowBook(userId, bookId);
            return Response.Builder.success(borrow, "借阅成功");
          
        } catch (IllegalArgumentException e) {
            return Response.Builder.error("参数错误: " + e.getMessage());
        } catch (IllegalStateException e) {
            return Response.Builder.error("操作失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("借阅图书异常", e);
            return Response.Builder.error("系统错误，请稍后重试");
        }
    }
}
```

### 配置管理

#### 1. 配置文件组织

```properties
# config/database.properties
database.url=jdbc:mysql://localhost:3306/vcampus
database.username=vcampus
database.password=password
database.driver=com.mysql.cj.jdbc.Driver
database.pool.maxSize=20
database.pool.minSize=5

# config/client.properties
server.host=localhost
server.port=8080
client.timeout=10000
client.retryCount=3
```

#### 2. 配置读取工具

```java
public class ConfigUtils {
    private static final Properties dbConfig = loadConfig("database.properties");
    private static final Properties clientConfig = loadConfig("client.properties");
  
    public static String getDatabaseUrl() {
        return dbConfig.getProperty("database.url");
    }
  
    public static String getServerHost() {
        return clientConfig.getProperty("server.host", "localhost");
    }
  
    private static Properties loadConfig(String filename) {
        Properties props = new Properties();
        try (InputStream is = ConfigUtils.class.getClassLoader()
                .getResourceAsStream(filename)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            log.warn("无法加载配置文件: {}", filename, e);
        }
        return props;
    }
}
```

### 测试策略

#### 1. 单元测试

```java
public class LibraryServiceTest {
    private LibraryService libraryService;
    private BookDao mockBookDao;
    private BookBorrowDao mockBorrowDao;
  
    @BeforeEach
    void setUp() {
        mockBookDao = Mockito.mock(BookDao.class);
        mockBorrowDao = Mockito.mock(BookBorrowDao.class);
        libraryService = new LibraryServiceImpl(mockBookDao, mockBorrowDao);
    }
  
    @Test
    void testBorrowBook_Success() throws Exception {
        // 准备测试数据
        Book book = new Book();
        book.setId(1L);
        book.setAvailableCount(5);
      
        when(mockBookDao.findById(1L)).thenReturn(book);
        when(mockBorrowDao.save(any(BookBorrow.class))).thenReturn(new BookBorrow());
      
        // 执行测试
        BookBorrow result = libraryService.borrowBook(100L, 1L);
      
        // 验证结果
        assertNotNull(result);
        verify(mockBookDao).update(book);
        assertEquals(4, book.getAvailableCount());
    }
  
    @Test
    void testBorrowBook_BookNotFound() {
        when(mockBookDao.findById(1L)).thenReturn(null);
      
        Exception exception = assertThrows(Exception.class, 
            () -> libraryService.borrowBook(100L, 1L));
      
        assertEquals("图书不存在", exception.getMessage());
    }
}
```

#### 2. 集成测试

```java
public class LibraryIntegrationTest {
    @Test
    void testBorrowAndReturnWorkflow() throws Exception {
        // 1. 搜索图书
        Map<String, Object> conditions = Map.of("title", "Java");
        List<Book> books = libraryService.searchBooks(conditions);
        assertFalse(books.isEmpty());
      
        // 2. 借阅图书
        Book book = books.get(0);
        int originalCount = book.getAvailableCount();
        BookBorrow borrow = libraryService.borrowBook(USER_ID, book.getId());
        assertNotNull(borrow);
      
        // 3. 验证库存减少
        Book updatedBook = libraryService.getBookById(book.getId());
        assertEquals(originalCount - 1, updatedBook.getAvailableCount());
      
        // 4. 归还图书
        BookBorrow returned = libraryService.returnBook(borrow.getId());
        assertEquals(BorrowStatus.RETURNED, returned.getStatus());
      
        // 5. 验证库存恢复
        Book finalBook = libraryService.getBookById(book.getId());
        assertEquals(originalCount, finalBook.getAvailableCount());
    }
}
```

### 日志和调试

#### 1. 日志配置

```java
// 使用Lombok的@Slf4j注解
@Slf4j
public class LibraryServiceImpl implements LibraryService {
    @Override
    public BookBorrow borrowBook(Long userId, Long bookId) throws Exception {
        log.info("开始借阅图书: userId={}, bookId={}", userId, bookId);
      
        try {
            // 业务逻辑
            BookBorrow result = // ... 执行借阅
          
            log.info("借阅成功: borrowId={}", result.getId());
            return result;
          
        } catch (Exception e) {
            log.error("借阅失败: userId={}, bookId={}", userId, bookId, e);
            throw e;
        }
    }
}
```

#### 2. 调试技巧

```java
// 开发环境调试日志
public class DebugUtils {
    public static void logRequest(Request request) {
        if (log.isDebugEnabled()) {
            log.debug("收到请求: uri={}, params={}", 
                request.getUri(), 
                JsonUtils.toJson(request.getParams()));
        }
    }
  
    public static void logResponse(Response response) {
        if (log.isDebugEnabled()) {
            log.debug("发送响应: status={}, message={}", 
                response.getStatus(), 
                response.getMessage());
        }
    }
}

## 🐛 调试技巧

### 查看网络通信
服务器和客户端都会在DEBUG级别输出详细的网络通信日志。

### 查看路由注册
服务器启动时会显示所有注册的路由信息和权限要求。

### 数据库调试
使用 `DatabaseTest.java` 测试数据库连接和基本操作。

### 客户端界面调试
客户端提供了详细的日志输出，包括用户操作和界面状态变化。



## 🔒 安全特性

### 密码安全
- 密码加密存储（SHA-256 + 随机盐值）
- 实时密码强度检测（弱/中等/强/很强）
- 密码复杂度要求：长度≥8，包含至少两种字符类型
- 密码显示/隐藏功能
- 明文密码自动迁移：系统会自动将旧的明文密码迁移到加密格式
- 批量密码迁移工具：支持对所有用户密码进行批量加密处理

### 会话管理
- 基于Session的用户状态管理
- 自动会话超时
- 安全的登录/登出机制

### 权限控制
- 基于角色的访问控制（RBAC）
- 接口级权限验证
- 前端界面权限控制

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 遵循项目代码规范和UI设计原则
4. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
5. 推送到分支 (`git push origin feature/AmazingFeature`)
6. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 开发团队

- **VCampus Team** - *项目开发与维护*

## 🙏 致谢

- **Netty** - 高性能网络通信框架
- **FlatLAF** - 现代化Swing外观主题
- **MyBatis** - 优秀的持久层框架
- **MySQL** - 可靠的关系型数据库
- **Lombok** - 简化Java代码编写
- **Gradle** - 强大的构建工具
