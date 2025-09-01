# Common模块最终规格说明文档

## 1. 概述

### 1.1 模块定位
Common模块是VCampus项目的**通用基础设施层**，提供真正可复用的、与业务逻辑无关的通用组件。该模块遵循"最小依赖"原则，只包含真正通用的基础设施代码。

### 1.2 设计原则
- **单一职责**：每个组件职责明确，功能聚焦
- **开闭原则**：对扩展开放，对修改关闭
- **依赖倒置**：依赖抽象而非具体实现
- **最小依赖**：不依赖任何业务逻辑或具体实现

## 2. 模块架构

### 2.1 整体结构
```
common模块（通用基础设施）
├── dao/           # 数据访问基础设施
├── db/            # 数据库连接基础设施
├── entity/        # 通用实体接口
├── enums/         # 通用枚举类型
├── message/       # 通用消息结构
└── util/          # 通用工具类
```

### 2.2 分层架构
```
应用层 (Controller/Service)
    ↓
业务层 (Server模块)
    ↓
通用基础设施层 (Common模块) ← 当前模块
    ↓
数据库层 (MySQL)
```

## 3. 核心组件详解

### 3.1 数据访问基础设施 (dao包)

#### 3.1.1 IDao接口
- **文件路径**：`dao/IDao.java`
- **代码行数**：53行
- **核心功能**：定义基础CRUD操作契约
- **设计模式**：接口隔离原则

```java
public interface IDao<T, ID> {
    Optional<T> findById(ID id);           // 根据ID查找
    List<T> findAll();                     // 查找所有
    T save(T entity);                      // 保存（新增或更新）
    List<T> saveAll(List<T> entities);     // 批量保存
    void delete(T entity);                 // 删除实体
    void deleteById(ID id);                // 根据ID删除
    long count();                          // 统计总数
    boolean existsById(ID id);             // 检查是否存在
}
```

#### 3.1.2 AbstractDao抽象类
- **文件路径**：`dao/AbstractDao.java`
- **代码行数**：232行
- **核心功能**：实现通用CRUD逻辑
- **设计模式**：模板方法模式

**核心特性**：
- 提供完整的CRUD操作实现
- 支持条件查询和分页查询
- 自动资源管理（try-with-resources）
- 统一的异常处理机制

**抽象方法**：
```java
protected abstract String getInsertSql();                    // 插入SQL
protected abstract String getUpdateSql();                    // 更新SQL
protected abstract void setInsertParams(PreparedStatement stmt, T entity);  // 设置插入参数
protected abstract void setUpdateParams(PreparedStatement stmt, T entity);  // 设置更新参数
protected abstract ID getIdValue(T entity);                  // 获取ID值
protected abstract void setIdValue(T entity, Object id);     // 设置ID值
```

#### 3.1.3 EntityMapper接口
- **文件路径**：`dao/EntityMapper.java`
- **代码行数**：21行
- **核心功能**：数据库结果集到实体的映射
- **设计模式**：策略模式

```java
@FunctionalInterface
public interface EntityMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
```

#### 3.1.4 QueryCondition类
- **文件路径**：`dao/QueryCondition.java`
- **代码行数**：127行
- **核心功能**：动态查询条件构建
- **设计模式**：建造者模式

**支持的操作**：
- 等于：`eq(String field, Object value)`
- 模糊查询：`like(String field, String value)`
- 大于：`gt(String field, Object value)`
- 小于：`lt(String field, Object value)`
- 排序：`orderByAsc(String field)`, `orderByDesc(String field)`

**使用示例**：
```java
QueryCondition condition = QueryCondition.create()
    .eq("userType", "student")
    .like("name", "张")
    .orderByAsc("cardNum");
```

#### 3.1.5 PageResult类
- **文件路径**：`dao/PageResult.java`
- **代码行数**：54行
- **核心功能**：分页结果封装
- **设计模式**：数据传输对象(DTO)

**核心属性**：
```java
private List<T> content;        // 当前页数据
private int page;               // 当前页码
private int size;               // 每页大小
private long totalElements;     // 总记录数
private int totalPages;         // 总页数
```

**便捷方法**：
```java
public boolean hasPrevious() { return page > 0; }
public boolean hasNext() { return page < totalPages - 1; }
```

### 3.2 数据库连接基础设施 (db包)

#### 3.2.1 DbHelper类
- **文件路径**：`db/DbHelper.java`
- **代码行数**：151行
- **核心功能**：数据库连接管理和SQL执行
- **设计模式**：单例模式 + 工具类模式

**核心功能**：
- 多数据源支持
- HikariCP连接池管理
- 统一的SQL执行接口
- 自动资源管理

**主要方法**：
```java
public static void init()                    // 初始化连接池
public static Connection getConnection()     // 获取数据库连接
public static ResultSet executeQuery(String sql, Object... params)    // 执行查询
public static int executeUpdate(String sql, Object... params)         // 执行更新
public static void close()                   // 关闭连接池
```

#### 3.2.2 数据源实现 (impl包)
- **文件路径**：`db/impl/MysqlDataSource.java`
- **核心功能**：MySQL数据源配置
- **技术栈**：HikariCP连接池

### 3.3 通用实体接口 (entity包)

#### 3.3.1 IEntity接口
- **文件路径**：`entity/IEntity.java`
- **代码行数**：64行
- **核心功能**：实体基类接口
- **设计模式**：接口隔离原则

**核心方法**：
```java
String toJson();                    // 转换为JSON字符串
<T> T fromJson(String json, Class<T> clazz);  // 从JSON反序列化
Map<String, Object> toMap();       // 转换为Map
void fromMap(Map<String, Object> map);  // 从Map反序列化
```

#### 3.3.2 User实体类
- **文件路径**：`entity/User.java`
- **代码行数**：47行
- **核心功能**：用户基础信息模型
- **实现接口**：IEntity

**核心属性**：
```java
private String cardNum; // 卡号（主键）
private String password;           // 密码
private String name;               // 真实姓名
private Gender gender;             // 性别
private String phone;              // 手机号
private String userType;           // 用户类型
```

### 3.4 通用枚举类型 (enums包)

#### 3.4.1 Gender枚举
- **文件路径**：`enums/Gender.java`
- **核心功能**：性别枚举
- **实现接口**：LabelledEnum

**枚举值**：
- `MALE`：男性
- `FEMALE`：女性
- `UNSPECIFIED`：未指定

#### 3.4.2 LabelledEnum接口
- **文件路径**：`enums/LabelledEnum.java`
- **核心功能**：带标签的枚举接口
- **设计模式**：策略模式

**核心方法**：
```java
String getLabel();                                    // 获取标签
static <T extends LabelledEnum> T findByLabel(String label);  // 根据标签查找
```

### 3.5 通用消息结构 (message包)

#### 3.5.1 Request类
- **文件路径**：`message/Request.java`
- **核心功能**：客户端请求封装
- **设计模式**：数据传输对象

**核心属性**：
```java
private String id;                 // 请求ID
private String uri;                // 请求URI
private Object data;               // 请求数据
private Session session;           // 会话信息
```

#### 3.5.2 Response类
- **文件路径**：`message/Response.java`
- **核心功能**：服务器响应封装
- **设计模式**：建造者模式

**核心属性**：
```java
private String id;                 // 响应ID
private boolean success;           // 是否成功
private String message;            // 响应消息
private Object data;               // 响应数据
```

#### 3.5.3 Session类
- **文件路径**：`message/Session.java`
- **核心功能**：用户会话信息
- **设计模式**：数据传输对象

**核心属性**：
```java
private String userId;             // 用户ID
private String userType;           // 用户类型
private long loginTime;            // 登录时间
```

### 3.6 通用工具类 (util包)

#### 3.6.1 DateUtils类
- **文件路径**：`util/DateUtils.java`
- **核心功能**：日期时间工具
- **设计模式**：工具类模式

**核心方法**：
```java
static String formatDate(Date date, String pattern);  // 格式化日期
static Date parseDate(String dateStr, String pattern); // 解析日期
static long getCurrentTimestamp();                     // 获取当前时间戳
```

#### 3.6.2 PasswordUtils类
- **文件路径**：`util/PasswordUtils.java`
- **核心功能**：密码处理工具
- **设计模式**：工具类模式

**核心方法**：
```java
static String hashPassword(String password);          // 密码哈希
static boolean verifyPassword(String password, String hash); // 密码验证
static String generateSalt();                         // 生成盐值
```

#### 3.6.3 Pair类
- **文件路径**：`util/Pair.java`
- **核心功能**：键值对容器
- **设计模式**：值对象模式

**核心属性**：
```java
private K first;    // 第一个元素
private V second;   // 第二个元素
```

## 4. 技术规格

### 4.1 代码统计

| 组件类别 | 文件数量 | 总代码行数 | 平均行数 | 说明 |
|----------|----------|------------|----------|------|
| 数据访问基础设施 | 5个 | 487行 | 97行 | 核心DAO框架 |
| 数据库连接基础设施 | 2个 | 151行 | 76行 | 数据库工具 |
| 通用实体接口 | 2个 | 111行 | 56行 | 实体基类 |
| 通用枚举类型 | 2个 | 约50行 | 25行 | 枚举定义 |
| 通用消息结构 | 3个 | 约100行 | 33行 | 消息封装 |
| 通用工具类 | 3个 | 约80行 | 27行 | 工具方法 |
| **总计** | **17个** | **约979行** | **58行** | **整体评估** |

### 4.2 设计质量评估

| 质量维度 | 评估结果 | 说明 | 改进建议 |
|----------|----------|------|----------|
| 单一职责 | ✅ 优秀 | 每个组件职责明确 | 无需改进 |
| 开闭原则 | ✅ 优秀 | 易于扩展新功能 | 无需改进 |
| 依赖倒置 | ✅ 优秀 | 依赖抽象而非具体 | 无需改进 |
| 接口隔离 | ✅ 优秀 | 接口功能聚焦 | 无需改进 |
| 里氏替换 | ✅ 优秀 | 子类可替换父类 | 无需改进 |

## 5. 使用指南

### 5.1 依赖关系
```gradle
// 其他模块依赖common模块
dependencies {
    implementation project(':common')
}
```

### 5.2 扩展新实体
```java
// 1. 实现IEntity接口
public class Book implements IEntity { ... }

// 2. 继承AbstractDao
public class BookDao extends AbstractDao<Book, Integer> { ... }

// 3. 实现EntityMapper
public class BookEntityMapper implements EntityMapper<Book> { ... }
```

### 5.3 使用通用工具
```java
// 使用QueryCondition构建查询
QueryCondition condition = QueryCondition.create()
    .eq("status", "active")
    .orderByAsc("name");

// 使用分页查询
PageResult<User> page = userDao.findPage(0, 10);

// 使用通用工具类
String hashedPassword = PasswordUtils.hashPassword("123456");
```

## 6. 架构优势

### 6.1 模块职责清晰
- **Common模块**：纯基础设施，可被任何项目复用
- **Server模块**：业务逻辑，包含具体的DAO实现
- **Client模块**：客户端界面，依赖Server模块

### 6.2 扩展性极强
- 新增实体不影响Common模块
- Common模块永远稳定，无需修改
- 符合开闭原则

### 6.3 维护成本低
- Common模块代码稳定
- 业务变更只影响Server模块
- 团队分工更清晰

### 6.4 符合设计原则
- **单一职责**：每个模块职责单一
- **开闭原则**：Common模块对扩展关闭，对修改开放
- **依赖倒置**：高层模块不依赖低层模块的具体实现

## 7. 未来扩展方向

### 7.1 短期扩展（1-3个月）
- 添加更多通用工具类
- 完善异常处理机制
- 增加日志记录功能

### 7.2 中期扩展（3-6个月）
- 支持更多数据库类型
- 添加缓存基础设施
- 引入配置管理功能

### 7.3 长期扩展（6个月以上）
- 支持分布式部署
- 添加监控和指标收集
- 支持微服务架构

## 8. 总结

Common模块作为VCampus项目的通用基础设施层，通过精心设计的架构和设计模式，实现了功能完整、性能优异、易于扩展的基础设施。该模块：

1. **职责明确**：只包含真正通用的、可复用的组件
2. **架构清晰**：明确的分层设计，职责分离
3. **扩展性强**：支持快速开发新功能
4. **维护方便**：代码结构清晰，易于理解和维护
5. **符合原则**：遵循软件工程的最佳实践

通过合理的模块划分和依赖管理，Common模块为整个项目提供了坚实的技术基础，支持项目的快速发展和长期维护。
