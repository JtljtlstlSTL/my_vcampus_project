# VCampus 虚拟校园项目

## 🎓 项目简介

VCampus是一个基于Java的虚拟校园管理系统，采用客户端-服务器架构，支持学生信息管理、课程管理、图书管理等功能。

## 🏗️ 项目架构

```
my-vcampus-project/
├── common/          # 公共模块 - 消息类、工具类
├── server/          # 服务端模块 - 业务逻辑、数据处理
├── client/          # 客户端模块 - 用户界面
└── docs/           # 文档目录
```

### 技术栈
- **后端**: Java 21 (LTS) + Netty + Hibernate
- **前端**: Java Swing + FlatLAF
- **构建工具**: Gradle 8.0+
- **通信协议**: JSON over TCP

## 🚀 快速开始

### 环境要求
- **JDK 21 (LTS)** - 必需，支持最新的语言特性和性能优化
- **Gradle 8.0+** - 必需，支持JDK21的现代构建系统
- **内存**: 建议4GB+ RAM
- **操作系统**: Windows 10+, macOS 10.15+, Linux

> 📖 **详细配置说明**: 请查看 [docs/JDK21_CONFIGURATION.md](docs/JDK21_CONFIGURATION.md) 了解完整的JDK21配置要求和最佳实践。

### 1. 编译项目

```bash
# 编译所有模块
./gradlew build

# 或者在Windows上
gradlew.bat build
```

### 2. 启动服务器

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
```

### 3. 启动客户端

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
- `auth/changePassword` - 修改密码 [需要登录]
- `auth/users` - 获取用户列表 [管理员权限]

### 学生管理接口
- `student/info` - 获取学生个人信息 [学生权限]
- `student/update` - 更新学生信息 [学生权限]
- `student/list` - 获取学生列表 [教师权限]
- `student/add` - 添加学生 [管理员权限]
- `student/delete` - 删除学生 [管理员权限]
- `student/stats` - 获取统计信息 [教师权限]

## 👥 测试账号

### 管理员账号
- 用户名: `admin`
- 密码: `admin123`
- 权限: 管理员

### 学生账号
- 用户名: `student001`
- 密码: `123456`
- 权限: 学生

### 教师账号
- 用户名: `teacher001`
- 密码: `123456`
- 权限: 教师

### 员工账号
- 用户名: `staff001`
- 密码: `123456`
- 权限: 员工

## 🎮 服务器控制台命令

启动服务器后，可以在控制台使用以下命令：

- `help` - 显示帮助信息
- `status` - 显示服务器状态
- `routes` - 显示所有路由
- `stats` - 显示服务器统计
- `reset` - 重置统计信息
- `stop` - 停止服务器

## 📝 消息格式

### 请求格式 (Request)
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "uri": "auth/login",
  "params": {
    "username": "admin",
    "password": "admin123"
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
    "userId": "admin",
    "userName": "管理员",
    "role": "admin"
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

### 添加新的权限角色

在Session类中的 `hasPermission` 方法中添加新的角色逻辑。

### 自定义客户端界面

在 `client/src/main/java/com/vcampus/client/ui/` 目录下创建新的界面类。

## 🐛 调试技巧

### 查看网络通信
服务器和客户端都会在DEBUG级别输出详细的网络通信日志。

### 查看路由注册
服务器启动时会显示所有注册的路由信息。

### 使用客户端测试
客户端提供了通用的请求测试界面，可以测试任何API接口。

## 📁 项目文件结构

```
my-vcampus-project/
├── build.gradle                 # 根构建配置
├── settings.gradle              # 模块配置
├── README.md                    # 项目说明
├── common/                      # 公共模块
│   ├── build.gradle
│   └── src/main/java/com/vcampus/common/
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
│       │   ├── SystemController.java
│       │   ├── AuthController.java
│       │   └── StudentController.java
│       ├── net/                     # 网络层
│       │   ├── NettyServer.java
│       │   └── ServerHandler.java
│       └── router/                  # 路由系统
│           └── Router.java
└── client/                          # 客户端模块
    ├── build.gradle
    └── src/main/java/com/vcampus/client/
        ├── ClientApplication.java   # 客户端启动类
        ├── net/                     # 网络客户端
        │   └── NettyClient.java
        ├── service/                 # 客户端服务
        │   └── ClientService.java
        └── ui/                      # 用户界面
            └── MainFrame.java
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者

- **VCampus Team** - *Initial work*

## 🙏 致谢

- Netty - 高性能网络通信框架
- FlatLAF - 现代化Swing外观
- Lombok - 简化Java代码
- Gradle - 强大的构建工具
