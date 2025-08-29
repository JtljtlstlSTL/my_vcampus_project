# 消息模块 (Message Module)

这个模块提供了完整的客户端-服务端通信的消息封装，包括请求(Request)、响应(Response)和会话(Session)管理。

## 📁 文件结构

```
common/message/
├── Request.java      # 请求类 - 客户端发送给服务端
├── Response.java     # 响应类 - 服务端返回给客户端  
├── Session.java      # 会话类 - 用户状态和权限管理
├── MessageUtils.java # 工具类 - 便捷的消息创建和处理方法
└── README.md         # 本说明文档
```

## 🚀 快速开始

### 1. 创建请求

```java
// 方式1：简单请求
Request request = new Request("user/login");

// 方式2：带参数的请求
Request request = new Request("user/login", 
    Map.of("username", "张三", "password", "123456"));

// 方式3：使用工具类
Request request = MessageUtils.createLoginRequest("张三", "123456");

// 方式4：链式调用
Request request = new Request("user/profile")
    .addParam("userId", "1001")
    .addParam("includeDetail", "true");
```

### 2. 处理响应

```java
// 创建成功响应
Response response = Response.Common.ok(userData);

// 创建错误响应
Response response = Response.Common.error("用户名不存在");

// 绑定到请求
response.bindToRequest(request);

// 检查响应状态
if (response.isSuccess()) {
    Object data = response.getData();
    // 处理成功数据...
} else {
    String error = response.getMessage();
    // 处理错误信息...
}
```

### 3. 会话管理

```java
// 创建用户会话
Session session = Session.create(1001, new String[]{"student", "monitor"});

// 检查权限
if (session.hasRole("student")) {
    // 用户是学生...
}

if (session.isAdmin()) {
    // 用户是管理员...
}

// 更新活跃时间
session.updateLastActiveTime();
```

## 📋 详细API说明

### Request类

#### 核心字段
- `id`: 请求唯一标识符 (UUID)
- `action`: 操作类型，如 "user/login", "admin/getUserList"
- `params`: 请求参数 Map<String, String>
- `session`: 当前用户会话信息
- `timestamp`: 请求时间戳

#### 主要方法
- `addParam(key, value)`: 添加参数
- `getParam(key)`: 获取参数
- `hasParam(key)`: 检查参数是否存在
- `isAnonymous()`: 是否为匿名请求
- `hasPermission(role)`: 检查当前用户权限

### Response类

#### 核心字段
- `id`: 对应的请求ID
- `status`: 响应状态 ("success" 或 "error")
- `message`: 提示信息
- `data`: 响应数据
- `session`: 更新后的会话信息
- `timestamp`: 响应时间戳

#### 静态工厂方法
- `Response.Common.ok()`: 成功响应
- `Response.Common.error(message)`: 错误响应
- `Response.Common.badRequest()`: 参数错误
- `Response.Common.unauthorized()`: 未授权
- `Response.Common.forbidden()`: 权限不足

### Session类

#### 核心字段
- `userId`: 用户ID
- `roles`: 用户角色数组
- `createTime`: 会话创建时间
- `lastActiveTime`: 最后活跃时间
- `valid`: 会话是否有效

#### 权限检查方法
- `hasRole(role)`: 检查单个权限
- `hasAnyRole(roles...)`: 检查任一权限
- `hasAllRoles(roles...)`: 检查所有权限
- `isAdmin()`: 是否为管理员
- `isStudent()`: 是否为学生
- `isTeacher()`: 是否为教师

#### 状态管理方法
- `isLoggedIn()`: 是否已登录
- `isValid()`: 会话是否有效
- `updateLastActiveTime()`: 更新活跃时间
- `invalidate()`: 设置会话失效
- `isTimeout(minutes)`: 检查是否超时

## 🎯 使用示例

### 完整的登录流程

```java
// === 客户端 ===
// 1. 创建登录请求
Request loginRequest = MessageUtils.createLoginRequest("张三", "123456");

// 2. 发送请求给服务端...
Response response = sendToServer(loginRequest);

// 3. 处理响应
if (response.isSuccess()) {
    // 保存会话信息
    Session userSession = response.getSession();
    System.out.println("登录成功！用户角色：" + userSession.getRoleDescription());
} else {
    System.out.println("登录失败：" + response.getMessage());
}

// === 服务端 ===
public Response handleLogin(Request request) {
    String username = request.getParam("username");
    String password = request.getParam("password");
    
    // 验证用户
    User user = validateUser(username, password);
    if (user != null) {
        // 创建会话
        Session session = Session.create(user.getId(), user.getRoles());
        
        // 返回成功响应
        return MessageUtils.createSuccessResponse(request, user)
            .withSession(session);
    } else {
        return MessageUtils.createErrorResponse(request, "用户名或密码错误");
    }
}
```

### 需要权限的操作

```java
// === 客户端 ===
Request request = new Request("admin/deleteUser")
    .addParam("targetUserId", "1002");
request.setSession(currentUserSession); // 带上当前会话

// === 服务端 ===
public Response handleDeleteUser(Request request) {
    // 权限检查
    if (!MessageUtils.checkPermission(request, "admin")) {
        return MessageUtils.createForbiddenResponse(request);
    }
    
    // 执行删除操作...
    String targetUserId = request.getParam("targetUserId");
    boolean success = userService.deleteUser(targetUserId);
    
    if (success) {
        return MessageUtils.createSuccessResponse(request, "删除成功");
    } else {
        return MessageUtils.createErrorResponse(request, "删除失败");
    }
}
```

## ⚙️ 配置和扩展

### 自定义权限角色

```java
// 在Session中添加自定义角色检查
public class Session {
    public boolean isLibraryStaff() {
        return hasRole("library_staff");
    }
    
    public boolean isFinanceStaff() {
        return hasRole("finance_staff");
    }
}
```

### 扩展Request参数

```java
// 添加便捷的参数类型转换
public class Request {
    public Integer getIntParam(String key) {
        String value = getParam(key);
        return value != null ? Integer.valueOf(value) : null;
    }
    
    public Boolean getBooleanParam(String key) {
        String value = getParam(key);
        return value != null ? Boolean.valueOf(value) : null;
    }
}
```

## 🔧 最佳实践

1. **请求命名规范**：使用 `模块/操作` 的格式，如 `user/login`, `admin/getUserList`

2. **参数验证**：在服务端处理前先验证必要参数
   ```java
   if (!request.hasParam("username") || !request.hasParam("password")) {
       return Response.Common.badRequest("用户名和密码不能为空");
   }
   ```

3. **会话更新**：在需要更新用户权限时，返回新的Session
   ```java
   // 用户角色变更后
   Session newSession = Session.create(userId, newRoles);
   return response.withSession(newSession);
   ```

4. **错误处理**：使用统一的错误响应格式
   ```java
   try {
       // 业务逻辑...
   } catch (Exception e) {
       return Response.Common.internalError("操作失败：" + e.getMessage());
   }
   ```

5. **权限检查**：在每个需要权限的操作前进行检查
   ```java
   if (!request.hasPermission("admin")) {
       return Response.Common.forbidden();
   }
   ```

这个消息模块为您的项目提供了完整的通信基础，支持安全的用户认证、灵活的权限控制和标准化的消息格式。
