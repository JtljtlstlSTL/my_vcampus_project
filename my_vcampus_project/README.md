# VCampus 虚拟校园管理系统

## 🎓 项目简介

VCampus是一个基于Java的虚拟校园管理系统，采用客户端-服务器架构，实现了完整的用户认证、角色管理和界面交互功能。系统支持学生端和教职工端的不同功能模块，提供了现代化的用户界面和安全的密码管理功能。

## 🏗️ 项目架构

```
my-vcampus-project/
├── common/          # 公共模块 - 消息类、实体类、工具类
├── server/          # 服务端模块 - 业务逻辑、数据处理、数据库操作
├── client/          # 客户端模块 - 用户界面、网络通信
├── config/          # 配置文件目录
├── database/        # 数据库脚本
└── docs/           # 文档目录
```

### 技术栈
- **后端**: Java 21 (LTS) + Netty + Hibernate
- **前端**: Java Swing + FlatLAF + 自定义UI组件
- **数据库**: MySQL + JPA/Hibernate
- **构建工具**: Gradle 8.0+
- **通信协议**: JSON over TCP
- **安全性**: 密码加密、会话管理、权限控制

## ✨ 核心功能

### 🔐 用户认证系统
- **多角色登录**: 支持学生、教职工、管理员等不同角色
- **安全认证**: 密码加密存储、会话管理
- **密码管理**: 
  - 实时密码强度检测
  - 密码显示/隐藏切换（支持眼睛图标）
  - 完整的密码要求提示
  - 密码修改功能

### 👨‍🎓 学生端功能
- **个人信息管理**: 查看和管理个人基本信息
- **角色显示**: 中文角色显示（"学生"）
- **课程管理**: 课程信息查看（开发中）
- **图书借阅**: 图书管理功能（开发中）
- **校园商城**: 商城功能（开发中）

### 👨‍🏫 教职工端功能
- **个人信息管理**: 查看和管理个人基本信息
- **教学管理**: 
  - 课程信息查看（只读模式）
  - 课程表查看（制度化管理，无增删改功能）
- **学生管理**: 学生信息查询和管理（开发中）
- **系统管理**: 管理员权限功能（开发中）

### 🖥️ 界面特性
- **现代化UI**: 基于FlatLAF的现代化界面设计
- **窗口控制**: 
  - 禁用关闭和最大化按钮（保留最小化）
  - 固定窗口大小
  - 自定义窗口行为
- **用户友好**: 
  - 实时状态栏显示
  - 统一的按钮布局
  - 响应式界面元素

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

# 2. 导入初始数据
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
./gradlew :client:run
```

## 📡 API接口文档

### 系统接口
- `system/heartbeat` - 心跳检测
- `system/info` - 服务器信息
- `system/time` - 服务器时间
- `system/echo` - 回显测试

### 认证接口
- `auth/login` - 用户登录
- `auth/logout` - 用户注销 [需要登录]
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

## 👥 测试账号

### 管理员账号
- 用户名: `admin`
- 密码: `admin123`
- 权限: 管理员
- 功能: 系统管理、用户管理

### 学生账号
- 用户名: `2021001` (学号)
- 密码: `student123`
- 权限: 学生
- 功能: 个人信息、课程查看

### 教师账号
- 用户名: `teacher001`
- 密码: `teacher123`
- 权限: 教师/教职工
- 功能: 教学管理、学生管理

### 员工账号
- 用户名: `staff001`
- 密码: `staff123`
- 权限: 员工
- 功能: 基本管理功能

> 💡 **提示**: 所有账号在首次登录后建议立即修改密码，系统支持实时密码强度检测。

## 🎮 服务器控制台命令

启动服务器后，可以在控制台使用以下命令：

- `help` - 显示帮助信息
- `status` - 显示服务器状态
- `routes` - 显示所有注册的路由
- `stats` - 显示服务器统计信息
- `users` - 显示在线用户
- `reset` - 重置统计信息
- `stop` - 优雅停止服务器

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

### 添加新的控制器

1. 在 `server/src/main/java/com/vcampus/server/controller/` 创建新的控制器类
2. 使用 `@RouteMapping` 注解标记方法：

```java
@RouteMapping(uri = "example/hello", role = "student", description = "示例接口")
public Response hello(Request request) {
    return Response.Builder.success("Hello World!");
}
```

### 添加新的实体类

1. 在 `common/src/main/java/com/vcampus/common/entity/` 创建实体类
2. 使用JPA注解进行数据库映射
3. 在对应的DAO类中实现数据访问逻辑

### 自定义客户端界面

1. 在 `client/src/main/java/com/vcampus/client/ui/` 目录下创建新的界面类
2. 继承JFrame或JDialog
3. 使用统一的UI组件和样式

### 界面组件规范

- **密码输入框**: 必须包含显示/隐藏切换按钮
- **窗口设置**: 固定大小、禁用关闭和最大化按钮
- **按钮布局**: 统一大小、合理间距
- **颜色主题**: 学生端使用橙色主题、教职工端使用蓝色主题

## 🐛 调试技巧

### 查看网络通信
服务器和客户端都会在DEBUG级别输出详细的网络通信日志。

### 查看路由注册
服务器启动时会显示所有注册的路由信息和权限要求。

### 数据库调试
使用 `DatabaseTest.java` 测试数据库连接和基本操作。

### 客户端界面调试
客户端提供了详细的日志输出，包括用户操作和界面状态变化。

## 📁 项目文件结构

```
my-vcampus-project/
├── build.gradle                 # 根构建配置
├── settings.gradle              # 模块配置
├── README.md                    # 项目说明
├── common/                      # 公共模块
│   ├── build.gradle
│   └── src/main/java/com/vcampus/common/
│       ├── entity/              # 实体类
│       │   ├── User.java
│       │   └── Student.java
│       ├── message/             # 消息类
│       │   ├── Request.java
│       │   ├── Response.java
│       │   └── Session.java
│       └── util/                # 工具类
│           ├── JsonUtils.java
│           └── StringUtils.java
├── server/                      # 服务端模块
│   ├── build.gradle
│   └── src/main/java/com/vcampus/server/
│       ├── ServerApplication.java    # 服务器启动类
│       ├── annotation/
│       │   └── RouteMapping.java    # 路由注解
│       ├── controller/              # 控制器
│       │   ├── AuthController.java
│       │   ├── SystemController.java
│       │   ├── StudentController.java
│       │   └── UserManagementController.java
│       ├── dao/                     # 数据访问层
│       │   ├── UserDAO.java
│       │   └── StudentDAO.java
│       ├── entity/                  # 服务端实体类
│       ├── service/                 # 业务逻辑层
│       │   └── UserService.java
│       ├── net/                     # 网络层
│       │   ├── NettyServer.java
│       │   └── ServerHandler.java
│       ├── router/                  # 路由系统
│       │   └── Router.java
│       └── utility/                 # 工具类
│           └── DatabaseHelper.java
├── client/                          # 客户端模块
│   ├── build.gradle
│   └── src/main/java/com/vcampus/client/
│       ├── ClientApplication.java   # 客户端启动类
│       ├── net/                     # 网络客户端
│       │   └── NettyClient.java
│       ├── service/                 # 客户端服务
│       │   └── ClientService.java
│       └── ui/                      # 用户界面
│           ├── LoginFrame.java      # 登录界面
│           ├── StudentFrame.java    # 学生主界面
│           ├── StaffFrame.java      # 教职工主界面
│           ├── MainFrame.java       # 主界面
│           └── component/           # UI组件
├── config/                          # 配置文件
│   ├── client.properties
│   └── database.properties
└── database/                        # 数据库
    └── init.sql                     # 初始化脚本
```

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
- **Hibernate** - 对象关系映射框架
- **MySQL** - 可靠的关系型数据库
- **Lombok** - 简化Java代码编写
- **Gradle** - 强大的构建工具

---

> 💡 **开发状态**: 核心功能已实现，部分模块持续开发中
> 
> 🔗 **技术支持**: 如遇问题请查看docs目录下的详细文档
> 
> 📧 **反馈建议**: 欢迎提交Issue或Pull Request
