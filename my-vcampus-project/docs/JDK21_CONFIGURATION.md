# JDK21 配置说明

## 📋 概述

VCampus项目基于Java 21 (LTS) 开发，充分利用了JDK21的新特性和性能优化。本文档详细说明了项目的JDK21配置要求和最佳实践。

## 🚀 JDK21 新特性应用

### 1. 语言特性
- **Record Patterns** - 用于数据类的模式匹配
- **Pattern Matching for Switch** - 增强的switch表达式
- **String Templates (Preview)** - 字符串模板功能
- **Virtual Threads** - 轻量级虚拟线程支持

### 2. 性能优化
- **G1GC改进** - 更好的垃圾收集性能
- **ZGC支持** - 低延迟垃圾收集器
- **字符串去重** - 减少内存占用
- **JIT编译器优化** - 更快的代码执行

## 🔧 环境配置

### 必需软件
```bash
# JDK版本要求
Java Version: 21.x.x
Java Vendor: Oracle OpenJDK 或 Eclipse Temurin

# 构建工具
Gradle Version: 8.0+
```

### 环境变量设置
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

# macOS/Linux
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### 验证安装
```bash
java -version
# 应该显示: openjdk version "21.x.x" 或 java version "21.x.x"

javac -version
# 应该显示: javac 21.x.x
```

## 📦 项目配置

### build.gradle 配置
```gradle
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // Lombok - 支持JDK21的版本
    compileOnly 'org.projectlombok:lombok:1.18.32'
    annotationProcessor 'org.projectlombok:lombok:1.18.32'
}
```

### IDE配置
#### IntelliJ IDEA
1. **Project Structure** → **Project** → **SDK**: 选择JDK21
2. **Project Structure** → **Modules** → **Language Level**: 选择21
3. **Settings** → **Build Tools** → **Gradle** → **Gradle JVM**: 选择JDK21

#### Eclipse
1. **Window** → **Preferences** → **Java** → **Installed JREs**: 添加JDK21
2. **Project Properties** → **Java Compiler**: 设置Compiler compliance level为21

#### VS Code
1. 安装Java Extension Pack
2. 设置`java.configuration.runtimes`指向JDK21
3. 设置`java.jdt.ls.java.home`为JDK21路径

## 🎯 运行时配置

### JVM参数优化
```bash
# 基础配置
-Xms512m -Xmx2g

# 垃圾收集器配置
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m

# 性能优化
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops

# 调试配置
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
```

### 应用启动脚本
```bash
# Windows (.bat)
@echo off
set JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
java %JAVA_OPTS% -jar vcampus-client.jar

# Unix/Linux (.sh)
#!/bin/bash
export JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
java $JAVA_OPTS -jar vcampus-client.jar
```

## 🔍 故障排除

### 常见问题

#### 1. 版本不匹配错误
```bash
# 错误信息
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.UnsupportedClassVersionError

# 解决方案
- 检查JAVA_HOME环境变量
- 确保使用JDK21而不是JRE
- 重新安装JDK21
```

#### 2. 编译错误
```bash
# 错误信息
javac: invalid source release: 21

# 解决方案
- 更新IDE的Java编译器版本
- 检查Gradle配置
- 确保使用JDK21的javac
```

#### 3. 运行时错误
```bash
# 错误信息
java.lang.NoClassDefFoundError

# 解决方案
- 检查classpath配置
- 确保所有依赖都已正确加载
- 检查模块路径配置
```

### 调试技巧
1. **使用JDK21的增强异常信息**
2. **启用详细的GC日志**
3. **使用JFR (Java Flight Recorder) 进行性能分析**
4. **利用虚拟线程进行并发调试**

## 📚 最佳实践

### 1. 代码风格
- 使用JDK21的新语言特性
- 遵循现代Java编码规范
- 利用Record类简化数据类
- 使用Pattern Matching简化条件逻辑

### 2. 性能优化
- 合理设置堆内存大小
- 选择合适的垃圾收集器
- 使用StringBuilder进行字符串拼接
- 避免不必要的对象创建

### 3. 测试策略
- 使用JUnit 5进行单元测试
- 利用JDK21的测试特性
- 进行性能基准测试
- 测试不同JVM参数配置

## 🔄 升级指南

### 从JDK17升级到JDK21
1. **备份项目**
2. **更新build.gradle配置**
3. **测试编译和运行**
4. **更新CI/CD配置**
5. **更新部署脚本**

### 兼容性检查
- 检查第三方库的JDK21兼容性
- 测试所有功能模块
- 验证性能指标
- 检查日志输出

## 📞 技术支持

### 官方资源
- [OpenJDK 21 Documentation](https://openjdk.org/projects/jdk/21/)
- [Oracle JDK 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)

### 社区支持
- Stack Overflow: `java`, `jdk21`, `gradle` 标签
- Reddit: r/java, r/gradle
- GitHub: 项目Issues页面

---

*最后更新：2024年8月29日*
*维护者：VCampus开发团队*
*JDK版本：21 (LTS)*
