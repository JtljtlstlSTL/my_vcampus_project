# DAO 使用示例文档

## 1. 概述

### 1.1 文档目的
本文档提供VCampus项目DAO框架的详细使用指南，包括基础操作、高级查询、扩展开发等各个方面，帮助开发者快速上手并正确使用DAO框架。

### 1.2 框架特点
- **类型安全**：泛型保证编译时类型检查
- **功能完整**：支持CRUD、条件查询、分页等常用操作
- **易于扩展**：新增实体只需继承AbstractDao并实现抽象方法
- **性能优化**：使用PreparedStatement防止SQL注入，支持连接池

## 2. 核心组件说明

### 2.1 组件架构

| 组件 | 作用 | 使用方式 | 说明 |
|------|------|----------|------|
| IDao | 定义基础CRUD操作契约 | 接口约束 | 所有DAO必须实现 |
| AbstractDao | 提供通用CRUD实现 | 继承使用 | 子类只需实现抽象方法 |
| EntityMapper | 数据库结果集映射 | 实现接口 | 将ResultSet转换为实体对象 |
| QueryCondition | 查询条件构建 | 链式调用 | 支持复杂查询条件组合 |
| PageResult | 分页结果封装 | 直接使用 | 包含分页信息和数据内容 |

### 2.2 核心接口定义

```java
// 基础CRUD接口
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

## 3. 基础使用示例

### 3.1 获取DAO实例

```java
// 方式1：直接获取（推荐）
UserDao userDao = UserDao.getInstance();

// 方式2：通过DatabaseManager获取
DatabaseManager dbManager = DatabaseManager.getInstance();
UserDao userDao = dbManager.getUserDao();
```

### 3.2 基础CRUD操作

#### 3.2.1 查询操作

```java
// 根据ID查询单个用户
Optional<User> userOpt = userDao.findById("100001");
if (userOpt.isPresent()) {
    User user = userOpt.get();
    System.out.println("用户姓名: " + user.getName());
    System.out.println("用户类型: " + user.getUserType());
} else {
    System.out.println("未找到用户");
}

// 查询所有用户
List<User> allUsers = userDao.findAll();
System.out.println("总用户数: " + allUsers.size());

// 检查用户是否存在
boolean exists = userDao.existsById(100001);
System.out.println("用户是否存在: " + exists);

// 统计用户总数
long totalCount = userDao.count();
System.out.println("用户总数: " + totalCount);
```

#### 3.2.2 保存操作

```java
// 创建新用户
User newUser = new User();
newUser.setName("张三");
newUser.setPassword("123456");
newUser.setGender(Gender.MALE);
newUser.setPhone("13800138000");
newUser.setUserType("student");

// 保存用户（自动判断新增或更新）
User savedUser = userDao.save(newUser);
System.out.println("用户保存成功，ID: " + savedUser.getCardNum());

// 更新用户信息
savedUser.setPhone("13900139000");
User updatedUser = userDao.save(savedUser);
System.out.println("用户信息更新成功");
```

#### 3.2.3 删除操作

```java
// 根据ID删除用户
userDao.deleteById("100001");
System.out.println("用户删除成功");

// 删除实体对象
User userToDelete = userDao.findById("100002").orElse(null);
if (userToDelete != null) {
    userDao.delete(userToDelete);
    System.out.println("用户删除成功");
}
```

#### 3.2.4 批量操作

```java
// 批量保存用户
List<User> users = new ArrayList<>();
users.add(new User("李四", "password1", Gender.FEMALE, "13800138001", "teacher"));
users.add(new User("王五", "password2", Gender.MALE, "13800138002", "student"));
users.add(new User("赵六", "password3", Gender.FEMALE, "13800138003", "student"));

List<User> savedUsers = userDao.saveAll(users);
System.out.println("批量保存成功，共保存: " + savedUsers.size() + " 个用户");
```

## 4. 高级查询功能

### 4.1 条件查询

#### 4.1.1 基础条件查询

```java
// 查找所有学生
QueryCondition studentCondition = QueryCondition.create()
    .eq("userType", "student");
List<User> students = userDao.findByCondition(studentCondition);
System.out.println("学生数量: " + students.size());

// 查找所有教师
QueryCondition teacherCondition = QueryCondition.create()
    .eq("userType", "teacher");
List<User> teachers = userDao.findByCondition(teacherCondition);
System.out.println("教师数量: " + teachers.size());
```

#### 4.1.2 复杂条件查询

```java
// 查找姓"张"的学生，按卡号升序排列
QueryCondition complexCondition = QueryCondition.create()
    .eq("userType", "student")
    .like("name", "张")
    .orderByAsc("cardNum");

List<User> zhangStudents = userDao.findByCondition(complexCondition);
System.out.println("姓张的学生数量: " + zhangStudents.size());

// 查找年龄大于20的用户，按姓名降序排列
QueryCondition ageCondition = QueryCondition.create()
    .gt("age", 20)
    .orderByDesc("name");

List<User> olderUsers = userDao.findByCondition(ageCondition);
System.out.println("年龄大于20的用户数量: " + olderUsers.size());
```

#### 4.1.3 支持的查询条件

| 条件类型 | 方法 | 示例 | 说明 |
|------|------|------|------|
| 等于 | `eq(String field, Object value)` | `.eq("userType", "student")` | 精确匹配 |
| 模糊查询 | `like(String field, String value)` | `.like("name", "张")` | 包含匹配 |
| 大于 | `gt(String field, Object value)` | `.gt("age", 20)` | 大于指定值 |
| 小于 | `lt(String field, Object value)` | `.lt("age", 30)` | 小于指定值 |
| 升序排序 | `orderByAsc(String field)` | `.orderByAsc("cardNum")` | 按字段升序 |
| 降序排序 | `orderByDesc(String field)` | `.orderByDesc("name")` | 按字段降序 |

### 4.2 分页查询

```java
// 分页查询所有用户，每页10条
int page = 0;        // 第1页（从0开始）
int size = 10;       // 每页10条

PageResult<User> pageResult = userDao.findPage(page, size);

// 分页信息
System.out.println("当前页: " + (pageResult.getPage() + 1));
System.out.println("每页大小: " + pageResult.getSize());
System.out.println("总记录数: " + pageResult.getTotalElements());
System.out.println("总页数: " + pageResult.getTotalPages());

// 分页导航
System.out.println("是否有上一页: " + pageResult.hasPrevious());
System.out.println("是否有下一页: " + pageResult.hasNext());

// 当前页数据
List<User> currentPageUsers = pageResult.getContent();
System.out.println("当前页用户数量: " + currentPageUsers.size());

// 遍历当前页数据
for (User user : currentPageUsers) {
    System.out.println("用户: " + user.getName() + " - " + user.getUserType());
}

// 分页查询特定条件的用户
QueryCondition condition = QueryCondition.create()
    .eq("userType", "student")
    .orderByAsc("name");

// 先执行条件查询
List<User> allStudents = userDao.findByCondition(condition);

// 手动分页（如果需要）
int studentPage = 0;
int studentSize = 5;
int startIndex = studentPage * studentSize;
int endIndex = Math.min(startIndex + studentSize, allStudents.size());

List<User> studentPageData = allStudents.subList(startIndex, endIndex);
PageResult<User> studentPageResult = new PageResult<>(
    studentPageData, studentPage, studentSize, allStudents.size()
);
```

## 5. 业务方法使用

### 5.1 UserDao特有方法

```java
// 根据手机号查找用户
Optional<User> userByPhone = userDao.findByPhone("13800138000");
if (userByPhone.isPresent()) {
    System.out.println("找到用户: " + userByPhone.get().getName());
}

// 根据用户类型查找用户
List<User> allStudents = userDao.findByUserType("student");
System.out.println("学生总数: " + allStudents.size());

// 根据姓名模糊查询
List<User> zhangUsers = userDao.findByNameLike("张");
System.out.println("姓张的用户数量: " + zhangUsers.size());

// 检查用户名是否可用
boolean isAvailable = userDao.isUsernameAvailable("100001");
System.out.println("用户名是否可用: " + isAvailable);

// 用户登录验证
Optional<User> authenticatedUser = userDao.authenticate("100001", "123456");
if (authenticatedUser.isPresent()) {
    System.out.println("登录成功: " + authenticatedUser.get().getName());
} else {
    System.out.println("登录失败：用户名或密码错误");
}
```

## 6. 数据库管理

### 6.1 初始化数据库

```java
// 应用启动时初始化
DatabaseManager dbManager = DatabaseManager.getInstance();
dbManager.initDatabase();
System.out.println("数据库连接初始化成功");
```

### 6.2 测试数据库连接

```java
// 测试数据库连接是否正常
if (dbManager.testConnection()) {
    System.out.println("数据库连接正常");
} else {
    System.err.println("数据库连接异常");
}
```

### 6.3 关闭数据库连接

```java
// 应用关闭时清理资源
dbManager.closeDatabase();
System.out.println("数据库连接已关闭");
```

## 7. 异常处理

### 7.1 常见异常类型

| 异常类型 | 原因 | 处理方式 | 示例 |
|------|------|----------|------|
| RuntimeException | 业务逻辑错误 | 捕获并处理 | 用户不存在、权限不足等 |
| SQLException | 数据库操作错误 | 自动转换 | 连接失败、SQL语法错误等 |

### 7.2 异常处理示例

```java
try {
    // 查询用户
    Optional<User> user = userDao.findById("100001");
    if (user.isPresent()) {
        System.out.println("用户: " + user.get().getName());
    } else {
        System.out.println("用户不存在");
    }
} catch (RuntimeException e) {
    // 处理业务异常
    System.err.println("查询失败: " + e.getMessage());
    // 记录日志、返回错误信息等
}

try {
    // 删除用户
    userDao.deleteById("100001");
    System.out.println("用户删除成功");
} catch (RuntimeException e) {
    // 处理删除异常
    if (e.getMessage().contains("未找到")) {
        System.err.println("要删除的用户不存在");
    } else {
        System.err.println("删除失败: " + e.getMessage());
    }
}
```

## 8. 扩展开发指南

### 8.1 创建新实体DAO

#### 8.1.1 步骤1：创建实体类

```java
public class Course implements IEntity {
    private Integer id;
    private String name;
    private Integer teacherId;
    private Integer credits;
    
    // 构造函数、getter/setter方法
    // 实现IEntity接口方法
}
```

#### 8.1.2 步骤2：创建EntityMapper

```java
public class CourseEntityMapper implements EntityMapper<Course> {
    @Override
    public Course map(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setName(rs.getString("name"));
        course.setTeacherId(rs.getInt("teacherId"));
        course.setCredits(rs.getInt("credits"));
        return course;
    }
}
```

#### 8.1.3 步骤3：创建DAO类

```java
public class CourseDao extends AbstractDao<Course, Integer> {
    private static CourseDao instance;
    
    private CourseDao() {
        super("courses", "id", new CourseEntityMapper());
    }
    
    public static synchronized CourseDao getInstance() {
        if (instance == null) {
            instance = new CourseDao();
        }
        return instance;
    }
    
    @Override
    protected String getInsertSql() {
        return "INSERT INTO courses (name, teacherId, credits) VALUES (?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSql() {
        return "UPDATE courses SET name = ?, teacherId = ?, credits = ? WHERE id = ?";
    }
    
    @Override
    protected void setInsertParams(PreparedStatement stmt, Course course) throws SQLException {
        stmt.setString(1, course.getName());
        stmt.setInt(2, course.getTeacherId());
        stmt.setInt(3, course.getCredits());
    }
    
    @Override
    protected void setUpdateParams(PreparedStatement stmt, Course course) throws SQLException {
        stmt.setString(1, course.getName());
        stmt.setInt(2, course.getTeacherId());
        stmt.setInt(3, course.getCredits());
        stmt.setInt(4, course.getId());
    }
    
    @Override
    protected Integer getIdValue(Course course) {
        return course.getId();
    }
    
    @Override
    protected void setIdValue(Course course, Object id) {
        course.setId((Integer) id);
    }
    
    // 添加业务方法
    public List<Course> findByTeacherId(Integer teacherId) {
        QueryCondition condition = QueryCondition.create()
            .eq("teacherId", teacherId)
            .orderByAsc("name");
        return findByCondition(condition);
    }
    
    public List<Course> findByCredits(Integer minCredits) {
        QueryCondition condition = QueryCondition.create()
            .gte("credits", minCredits)
            .orderByDesc("credits");
        return findByCondition(condition);
    }
}
```

#### 8.1.4 步骤4：在DatabaseManager中注册

```java
public class DatabaseManager {
    private static DatabaseManager instance;
    private final UserDao userDao;
    private final CourseDao courseDao;  // 新增
    
    private DatabaseManager() {
        this.userDao = UserDao.getInstance();
        this.courseDao = CourseDao.getInstance();  // 新增
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public UserDao getUserDao() {
        return userDao;
    }
    
    public CourseDao getCourseDao() {  // 新增
        return courseDao;
    }
}
```

### 8.2 使用新创建的DAO

```java
// 获取CourseDao实例
CourseDao courseDao = DatabaseManager.getInstance().getCourseDao();

// 创建新课程
Course newCourse = new Course();
newCourse.setName("Java程序设计");
newCourse.setTeacherId(100001);
newCourse.setCredits(4);

// 保存课程
Course savedCourse = courseDao.save(newCourse);
System.out.println("课程创建成功，ID: " + savedCourse.getId());

// 查询特定教师的课程
List<Course> teacherCourses = courseDao.findByTeacherId(100001);
System.out.println("教师课程数量: " + teacherCourses.size());

// 查询学分大于等于3的课程
List<Course> highCreditCourses = courseDao.findByCredits(3);
System.out.println("高学分课程数量: " + highCreditCourses.size());
```

## 9. 最佳实践

### 9.1 性能优化建议

| 优化策略 | 具体做法 | 效果 | 说明 |
|------|----------|------|------|
| 批量操作 | 使用saveAll()方法 | 高 | 减少数据库交互次数 |
| 条件查询 | 合理使用QueryCondition | 中 | 避免全表扫描 |
| 分页查询 | 使用findPage()方法 | 中 | 控制内存使用 |
| 连接管理 | 使用try-with-resources | 高 | 自动资源管理 |

### 9.2 代码规范建议

| 规范类型 | 具体要求 | 示例 | 说明 |
|------|----------|------|------|
| 命名规范 | 方法名清晰明确 | `findByUserType()` | 便于理解方法用途 |
| 异常处理 | 统一异常处理 | 转换为RuntimeException | 提供清晰的错误信息 |
| 资源管理 | 使用try-with-resources | 自动关闭资源 | 避免资源泄漏 |
| 注释规范 | 关键方法添加注释 | 说明参数和返回值 | 提高代码可读性 |

### 9.3 安全建议

| 安全方面 | 具体措施 | 实现方式 | 说明 |
|------|----------|----------|------|
| SQL注入防护 | 使用PreparedStatement | 自动实现 | 防止恶意SQL注入 |
| 参数验证 | 输入参数检查 | 手动实现 | 确保数据有效性 |
| 权限控制 | 业务层权限检查 | 手动实现 | 控制数据访问权限 |
| 日志记录 | 关键操作日志 | 手动实现 | 便于审计和调试 |

## 10. 常见问题解答

### 10.1 问题1：如何实现复杂的多表关联查询？

**答案**：当前DAO框架主要支持单表操作，复杂关联查询可以通过以下方式实现：

```java
// 方式1：在业务层组合多个DAO查询
public List<CourseWithTeacher> getCoursesWithTeacher() {
    List<Course> courses = courseDao.findAll();
    List<CourseWithTeacher> result = new ArrayList<>();
    
    for (Course course : courses) {
        Optional<User> teacher = userDao.findById(course.getTeacherId());
        if (teacher.isPresent()) {
            CourseWithTeacher cwt = new CourseWithTeacher(course, teacher.get());
            result.add(cwt);
        }
    }
    return result;
}

// 方式2：扩展AbstractDao支持自定义SQL
public List<CourseWithTeacher> getCoursesWithTeacherCustom() {
    String sql = "SELECT c.*, u.name as teacherName FROM courses c " +
                 "LEFT JOIN users u ON c.teacherId = u.cardNum " +
                 "WHERE u.userType = 'teacher'";
    
    // 实现自定义查询逻辑
    // ...
}
```

### 10.2 问题2：如何实现事务管理？

**答案**：当前版本支持手动事务管理：

```java
// 手动事务管理示例
try (Connection conn = DbHelper.getConnection()) {
    // 开启事务
    conn.setAutoCommit(false);
    
    try {
        // 执行多个操作
        userDao.save(user1);
        userDao.save(user2);
        
        // 提交事务
        conn.commit();
        System.out.println("事务执行成功");
    } catch (Exception e) {
        // 回滚事务
        conn.rollback();
        System.err.println("事务执行失败，已回滚: " + e.getMessage());
        throw e;
    }
} catch (SQLException e) {
    System.err.println("数据库连接异常: " + e.getMessage());
}
```

### 10.3 问题3：如何优化大量数据的查询性能？

**答案**：可以通过以下方式优化：

```java
// 1. 使用分页查询避免一次性加载大量数据
PageResult<User> page = userDao.findPage(0, 1000); // 每页1000条

// 2. 使用条件查询减少数据量
QueryCondition condition = QueryCondition.create()
    .eq("userType", "student")
    .orderByAsc("cardNum");
List<User> students = userDao.findByCondition(condition);

// 3. 批量处理数据
List<User> allUsers = userDao.findAll();
int batchSize = 1000;
for (int i = 0; i < allUsers.size(); i += batchSize) {
    int endIndex = Math.min(i + batchSize, allUsers.size());
    List<User> batch = allUsers.subList(i, endIndex);
    // 处理这一批数据
    processBatch(batch);
}
```

## 11. 总结

本文档详细介绍了VCampus项目DAO框架的使用方法，包括：

1. **基础操作**：CRUD操作、批量处理
2. **高级查询**：条件查询、分页查询
3. **扩展开发**：创建新实体DAO的完整流程
4. **最佳实践**：性能优化、代码规范、安全建议
5. **常见问题**：实际开发中可能遇到的问题和解决方案

通过合理使用DAO框架，可以：
- 提高开发效率
- 保证代码质量
- 实现数据访问的统一管理
- 支持业务的快速扩展

建议开发者在实际使用中，根据具体业务需求选择合适的查询方式，并遵循最佳实践建议，确保代码的可维护性和性能。
