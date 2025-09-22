# VCampus 虚拟校园系统

一个基于 Java 的多模块项目，涵盖“服务端 + 客户端 + 公共模块”的完整校园业务示例。项目采用 Netty 自研通讯协议、MySQL 持久化、Swing 桌面 UI，并集成图像识别、音视频播放、Excel 批量导入/导出等能力。

> 模块：`common`（公共组件与实体） + `server`（业务与路由） + `client`（桌面客户端 UI）


## 功能概览

面向“学生端 + 教师端/管理员端”的校园一体化应用（节选）：

- 学生端
	- 成绩查询、加权平均分与 GPA 计算
	- 选课与课表/成绩查询
	- 图书馆借阅/归还/续借、书籍检索与推荐购置
	- 校园卡充值/消费/账单查询
	- 校园商店下单与购物车
	- 个人信息维护、头像管理
    - 人脸采集、识别
    - 云课堂视频播放
- 教师/管理员端
	- 课程管理与批量导入/成绩录入
	- 学生信息查询与编辑
	- 图书入库/借还管理、借阅规则配置、统计看板
	- 校园卡管理（充值、挂失等）
	- 商店商品/订单管理
    - 课程视频上传


## 项目展示

### 登录界面

<p>
	<img src="pictures/login.jpg" alt="登录界面" width="360" />
</p>

### 课程与功能入口（示意）

<p>
	<img src="pictures/login.jpg" alt="课程卡片-示例1" width="320" />
	<img src="pictures/login.jpg" alt="课程卡片-示例2" width="320" />
	<img src="pictures/login.jpg" alt="AI助手入口（图标示意）" width="120" />
	<img src="pictures/login.jpg" alt="管理员入口（图标示意）" width="120" />
</p>


## 技术栈

- Java 21（Gradle 多模块）
- Netty 4.1.x（自定义协议，Server/Client）
- MySQL 8.x + HikariCP（连接池）
- MyBatis（部分 Mapper/DAO）
- Gson（JSON 序列化）
- SLF4J + Logback（日志）
- Lombok（简化样板代码）
- Swing + FlatLaf（现代化桌面 UI）
- VLCJ + JNA（基于 libVLC 的视频播放）
- LWJGL OpenAL（音频）
- Apache POI（Excel 导入/导出）
- JFreeChart（可视化）
- OpenCV 4.1.10（人脸识别，已随库提供 DLL/JAR）


## 仓库结构

```
my_vcampus_project/
├─ build.gradle                 # 根构建，统一 JDK 21、依赖、IDE 设置
├─ settings.gradle              # 模块聚合（common/server/client）
├─ config/
│  ├─ client.properties         # 客户端连接与媒体路径配置
│  └─ database.properties       # 数据库连接（被 common 的数据源读取）
├─ database/
│  └─ vcampus.sql               # 完整数据库结构和演示数据
├─ common/                      # 公共实体/工具/数据源
├─ server/                      # 服务端（Netty + 路由 + 业务模块）
└─ client/                      # 客户端（Swing UI + Netty 客户端 + 多媒体）
```


## 环境要求

- 操作系统：Windows（推荐，已随项目提供 DLL），macOS/Linux 可自行适配
- JDK：21（已在 `build.gradle` 指定）
- MySQL：8.0+（建议 8.0.29+）
- Gradle：使用项目自带 Wrapper（无需预装 Gradle）
- 视频播放：本地 libVLC 运行库（Windows 已在 `client/libs/VLC` 准备了结构示例）


## 配置说明

1) 数据库配置（common 模块在运行期读取）

- 文件：`config/database.properties`
- 默认：
	- URL：`jdbc:mysql://localhost:3306/virtual_campus?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8`
	- 用户名/密码：`root/123456`（请按实际修改）
- 读取方式：`common` 模块的 `MysqlDataSource` 会从 classpath 下的 `config/database.properties` 读取。运行时请确保该文件在运行目录的类路径中（Gradle 运行已包含 `src/main/resources`，你可以将 `config` 复制/放置到资源路径或工作目录）。

2) 客户端配置

- 文件：`config/client.properties`
- 关键项：
	- `server.host=localhost`
	- `server.port=8081`（需与服务端一致）
	- `videos.path=my_vcampus_project\videos`（本地视频目录，可按需调整）
- 读取顺序（示例）：
	- `config/client.properties`（classpath）
	- 工作目录下 `./config/client.properties`
	- `client/config/client.properties` 或 `../config/client.properties`

3) VLC / OpenCV 运行库

- VLC（视频播放，VLCJ）：
	- 推荐将 libVLC 的 DLL 及其 plugins 放在 `client/libs/VLC`（或自定义目录）。
	- 客户端 `build.gradle` 已为 `gradle run` 设置：
		- `-Djna.library.path` 与 `-Djava.library.path` 指向 `${projectDir}/libs/vlc`
		- 运行任务会将该目录加入 PATH（Windows）。
- OpenCV：
	- 已随项目提供 `client/lib/opencv_java4110.dll` 与 `opencv-4110.jar`。
	- 保持二者同版本；如遇加载失败，确保 DLL 在 PATH 或 JVM 的 `java.library.path` 中。


## 数据库初始化

1) 在 MySQL 中创建并导入示例数据（Windows PowerShell）：

```powershell
# 登录 MySQL（按需修改账号）
mysql -u root -p

# 在 MySQL 提示符内执行（注意分号；或使用 SOURCE 执行完整脚本）
CREATE DATABASE virtual_campus CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE virtual_campus;
SOURCE "database/vcampus.sql";
```

2) 确认 `config/database.properties` 的 `db.url/db.username/db.password` 与实际一致。


## 构建与运行（Windows PowerShell）

建议从仓库根目录执行以下命令（使用 Wrapper）：

1) 清理与编译

```powershell
./gradlew.bat clean build
```

2) 启动服务端（默认端口 8081，可在命令行传参覆盖）

```powershell
# 方式 A：默认端口
./gradlew.bat :server:run

# 方式 B：指定端口（例如 9090）
./gradlew.bat :server:run --args "9090"
```

启动后，服务端会输出路由注册信息和控制台提示（输入 `help/status/routes/stats/reset/quit` 等命令）。

3) 启动客户端

```powershell
./gradlew.bat :client:run
```

首次运行请确认：

- 客户端的 `config/client.properties` 中 `server.host` 与 `server.port` 指向已启动的服务端。
- VLC 本地库路径正确（若使用视频模块）。

4) 可分发包

`application` 插件支持生成分发包：

```powershell
# 生成 server 与 client 的可运行分发（包含依赖与启动脚本）
./gradlew.bat :server:installDist :client:installDist

# 产物位置示例
# server/build/install/vcampus-server-temp/
# client/build/install/vcampus-client/
```

> 注意：`server` 的 `jar` 任务已将依赖打入（fat jar），而 `client` 更推荐通过 `installDist` 或 `distZip` 分发。


## 登录账号与角色

- 演示数据已在 `database/vcampus.sql` 中插入了若干用户（字段 `tbluser.cardNum`）。
- 由于密码采用加盐哈希存储，仓库不包含明文口令。常见演示口令为 `123456`（如无法登录，请联系数据库维护者重置密码，或在测试环境自行插入可用账号）。
- 登录框支持使用学工号/一卡通号作为用户名（`cardNum`）。


## 关键代码入口

- 服务端入口：`server/src/main/java/com/vcampus/server/ServerApplication.java`
	- 默认端口：`8081`（可通过 `--args` 或程序参数覆盖）
	- Netty 启动类：`com.vcampus.server.core.net.NettyServer`
	- 路由器：`com.vcampus.server.core.common.router.Router`（自动扫描 `@RouteMapping`，并带手工兜底注册）
	- 数据源初始化：`com.vcampus.common.db.DbHelper#init()` → `MysqlDataSource`
- 客户端入口：`client/src/main/java/com/vcampus/client/ClientApplication.java`
	- UI 主题：FlatLaf
	- 登录界面：`core/ui/login/LoginFrame`
	- 通讯客户端：`core/net/NettyClient`（与服务端 LineBasedFrame + JSON 通讯，对齐）


## 常见问题与排障（FAQ）

1) 无法连接数据库 / 启动时提示 `Database connection test failed`

- 检查 `config/database.properties` 是否被运行时加载（classpath 下是否存在）。
- 确认 MySQL 正在监听（Windows：`netstat -ano | findstr :3306`）。
- 若为远程数据库，请参考 `ServerConnection.md` 配置远程访问、防火墙与 SSH 隧道。

2) 客户端连接失败（超时或拒绝）

- 确认服务端已启动，并监听正确端口（服务端控制台 `status` 可查看）。
- 确认客户端 `server.host/server.port` 与服务端一致。
- 本地防火墙可能阻止 8081 端口：可临时放行测试后再收紧策略。

3) 视频播放报错：找不到 VLC 本地库 / JNA 加载失败

- 将 libVLC 的 `dll` 与 `plugins` 放在 `client/libs/VLC` 下；或将其所在目录加入系统 PATH。
- Gradle 运行任务会自动将 `client/libs/vlc` 加入 PATH（Windows）。若自定义目录，请同步修改 `client/build.gradle` 的 JVM 参数与 PATH 注入逻辑。

4) OpenCV 加载失败：`UnsatisfiedLinkError: opencv_java4110`/`java.library.path`

- 确认 `client/lib/opencv_java4110.dll` 与 `opencv-4110.jar` 版本匹配、路径正确。
- 将 DLL 目录加入 PATH 或以 JVM 参数 `-Djava.library.path` 指定。

5) 端口被占用

- 更换一个端口启动服务端：`./gradlew.bat :server:run --args "9090"`，并同步修改客户端配置。

6) Lombok 编译报错

- IDE 请启用注解处理，并安装 Lombok 插件。项目已在 `build.gradle` 配置 `annotationProcessor`。


## 开发建议

- 新增服务端路由时：在对应模块的 `controller` 中添加方法并标注 `@RouteMapping`，路由器会自动扫描；必要时可参考 `Router#registerManualRoutes()` 的手工兜底注册。
- 统一使用 `common` 模块中的消息与实体（例如 `Request/Response/Session`）。
- 涉及数据库的改动请同步更新 `database/vcampus.sql`，并在 README 标注迁移步骤。


## 许可

本仓库未显式声明开源许可。若需开源或二次分发，请在团队内确认后补充合适的 LICENSE。


## 致谢

- Netty、VLCJ、LWJGL、Apache POI、JFreeChart、OpenCV 等优秀开源项目
- 以及所有为本项目贡献代码与测试的同学


---

如在使用中遇到任何问题，欢迎提交 Issue 或通过评论补充信息，我会继续完善文档与引导脚本。