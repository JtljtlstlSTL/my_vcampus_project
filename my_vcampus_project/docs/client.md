# VCampus 客户端模块文档

## 📁 目录结构

```
client/
├── build.gradle                    # 客户端模块构建配置
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── vcampus/
│       │           └── client/
│       │               ├── ClientApplication.java    # 客户端主程序入口
│       │               ├── service/                  # 服务层
│       │               │   └── ClientService.java    # 客户端服务类
│       │               ├── ui/                       # 用户界面层
│       │               │   ├── LoginFrame.java       # 登录界面 ✨新增
│       │               │   └── MainFrame.java        # 主界面
│       │               └── net/                      # 网络通信层
│       └── resources/               # 资源文件
│           └── figures/             # 图片资源 ✨新增
│               ├── logo.png         # 项目logo
│               ├── 1.jpg            # 登录背景图片
│               ├── close1.png       # 关闭按钮图标
│               ├── minimize.png     # 最小化按钮图标
│               ├── eye_open.png     # 密码可见图标
│               └── eye_close.png    # 密码隐藏图标
```

## 🚀 启动流程

### 修改前
```
ClientApplication.main() → MainFrame → 直接显示主界面
```

### 修改后 ✨
```
ClientApplication.main() → LoginFrame → 用户登录 → MainFrame → 显示主界面
```

## 📋 核心类说明

### 1. ClientApplication.java - 客户端主程序入口

**职责：** 程序启动入口，负责初始化系统属性和启动GUI

**主要修改：**
- 将启动流程从直接显示MainFrame改为先显示LoginFrame
- 修复了@Slf4j在静态方法中的使用问题

**关键代码：**
```java
// 启动GUI - 修改前
SwingUtilities.invokeLater(() -> {
    MainFrame mainFrame = new MainFrame();
    mainFrame.setVisible(true);
});

// 启动GUI - 修改后 ✨
SwingUtilities.invokeLater(() -> {
    LoginFrame loginFrame = new LoginFrame();
    loginFrame.setVisible(true);
});
```

**核心方法：**
- `main()` - 程序入口点
- `printBanner()` - 打印启动横幅
- `setupSystemProperties()` - 设置系统属性
- `setupLookAndFeel()` - 设置UI外观

### 2. LoginFrame.java - 登录界面 ✨新增

**职责：** 提供用户登录界面，验证用户身份后跳转到主界面

**设计特点：**
- 基于VirtualCampusSystem的LoginUI设计
- 左右分栏布局（左侧背景图片，右侧登录表单）
- 无边框窗口设计
- 自定义关闭和最小化按钮

**UI组件结构：**
```
LoginFrame
├── JLayeredPane (分层面板)
│   ├── JSplitPane (分割面板)
│   │   ├── 左侧面板 (背景图片)
│   │   └── 右侧面板 (登录表单)
│   ├── 关闭按钮 (右上角)
│   ├── 最小化按钮 (右上角)
│   └── 密码可见性切换按钮
```

**核心方法：**
- `initializeUI()` - 初始化UI组件
- `createLeftPanel()` - 创建左侧图片面板
- `createRightPanel()` - 创建右侧表单面板
- `createTopRightButtons()` - 创建右上角按钮
- `createPasswordToggleButton()` - 创建密码切换按钮
- `handleLogin()` - 处理登录逻辑
- `openMainFrame()` - 打开主界面

**登录流程：**
1. 用户输入用户名和密码
2. 点击登录按钮或按回车键
3. 验证表单数据（非空检查）
4. 模拟登录成功
5. 显示成功消息
6. 自动跳转到主界面

### 3. MainFrame.java - 主界面

**职责：** 应用程序的主要工作界面

**功能特性：**
- 服务器连接管理
- 请求/响应处理
- 心跳检测
- 状态显示

## 🔧 技术实现细节

### 系统要求
- **JDK版本：** Java 21 (LTS) - 利用最新的语言特性和性能优化
- **构建工具：** Gradle 8.0+ - 支持JDK21的现代构建系统
- **内存要求：** 建议4GB+ RAM，支持G1GC垃圾收集器

### UI框架
- **Swing** - 主要GUI框架，在JDK21下性能更优
- **FlatLaf** - 现代化外观主题，支持高DPI显示
- **JLayeredPane** - 分层布局管理
- **JSplitPane** - 分割面板布局
- **GridBagLayout** - 网格包布局管理器

### 资源管理
- 使用`ClassLoader.getResource()`加载资源文件
- 异常处理确保资源加载失败时的降级方案
- 支持图片和图标资源的动态加载

### 事件处理
- 按钮点击事件
- 密码字段回车键事件
- 窗口状态变化事件

## 📝 代码实现要点

### 1. 分层布局实现
```java
// 使用分层面板实现右上角按钮
JLayeredPane layeredPane = new JLayeredPane();
layeredPane.setPreferredSize(new Dimension(710, 400));

// 使用JSplitPane分割左右面板
JSplitPane splitPane = new JSplitPane();
splitPane.setBounds(0, 0, 710, 400);
splitPane.setDividerLocation(450);
splitPane.setEnabled(false);
```

### 2. 资源加载与异常处理
```java
try {
    ImageIcon icon = new ImageIcon(getClass().getResource("/figures/1.jpg"));
    imageLabel.setIcon(icon);
} catch (Exception e) {
    log.warn("背景图片加载失败，使用文字替代");
    // 降级方案：使用文字替代图片
    imageLabel.setText("VCampus");
}
```

### 3. 密码可见性切换
```java
btnTogglePwd.addActionListener(e -> {
    if (isPasswordVisible) {
        txtPassword.setEchoChar('*');
        btnTogglePwd.setIcon(eyeCloseIcon);
        isPasswordVisible = false;
    } else {
        txtPassword.setEchoChar((char) 0);
        btnTogglePwd.setIcon(eyeOpenIcon);
        isPasswordVisible = true;
    }
});
```

### 4. 表单验证
```java
private void handleLogin() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "用户名和密码不能为空", 
            "登录错误", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    // 处理登录逻辑...
}
```

## 🎨 样式配置

### 颜色主题
- 主色调：`new Color(255, 127, 80)` (橙红色)
- 背景色：`new Color(248, 249, 250)` (浅灰色)
- 按钮颜色：
  - 登录按钮：橙红色背景，白色文字
  - 注册按钮：灰色背景，白色文字

### 字体设置
- 标题：微软雅黑，粗体，20号
- 副标题：微软雅黑，普通，8号
- 输入框：微软雅黑，普通，14号

### 窗口设置
- 尺寸：710x400像素
- 居中显示
- 无边框设计

## 🔄 数据流

### 登录流程数据流
```
用户输入 → 表单验证 → 登录处理 → 成功反馈 → 界面切换
   ↓           ↓         ↓         ↓         ↓
LoginFrame → 验证逻辑 → 模拟登录 → 消息框 → MainFrame
```

### 组件通信
- LoginFrame ↔ MainFrame：通过`openMainFrame()`方法
- UI组件 ↔ 事件处理器：通过ActionListener接口
- 资源文件 ↔ UI组件：通过ClassLoader加载

## 🚧 待完善功能

### 当前状态
- ✅ 登录界面UI完成
- ✅ 基本表单验证
- ✅ 界面跳转逻辑
- ✅ 资源文件集成

### 待实现功能
- [ ] 真实用户认证系统
- [ ] 记住密码功能
- [ ] 用户注册功能
- [ ] 密码强度验证
- [ ] 多语言支持
- [ ] 主题切换

## 📚 相关文档

- `../LOGIN_INTEGRATION_README.md` - 登录UI集成详细说明
- `build.gradle` - 项目依赖配置
- VirtualCampusSystem项目 - 登录UI设计参考

## 🔍 调试与维护

### 常见问题
1. **资源文件加载失败** - 检查文件路径和ClassLoader
2. **UI显示异常** - 检查Swing组件初始化和事件绑定
3. **编译错误** - 检查import语句和依赖配置
4. **JDK版本不匹配** - 确保使用Java 21，检查JAVA_HOME环境变量

### 调试技巧
- 使用System.out.println()输出调试信息
- 检查异常堆栈信息
- 验证资源文件路径正确性
- 使用JDK21的新特性进行调试（如增强的异常信息、改进的GC日志等）

### JDK21特定配置
- 启用G1GC垃圾收集器：`-XX:+UseG1GC`
- 启用ZGC（实验性）：`-XX:+UseZGC`
- 启用虚拟线程（实验性）：`-XX:+EnableVirtualThreads`
- 启用字符串去重：`-XX:+UseStringDeduplication`

---

*最后更新：2024年8月29日*
*维护者：VCampus开发团队*
