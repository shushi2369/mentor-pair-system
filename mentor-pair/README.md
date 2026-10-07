# 师生导师双选信息管理系统（mentor-pair）

基于 Java + Spring Boot + MySQL 的师生导师双选信息管理系统。管理员维护账号与全局数据，导师导入成绩单、发布项目并处理学生的双选申请，学员选择导师、提交已完成的项目文件并获得导师指导，形成"选—收—交—导"完整业务闭环。

技术方案与验收标准详见上级目录《技术指导文档.md》。

## 技术栈

| 项 | 选型 |
|---|---|
| 开发语言 | Java（按 Java 8 语法编写，`--release 8` 编译） |
| 核心框架 | Spring Boot 2.7.18（内嵌 Tomcat，单 jar 部署） |
| 构建 | Maven 3.3.9 |
| 数据库 | MySQL 8.0（utf8mb4，库名 `mentor_pair`，启动自动建库建表+种子数据） |
| 持久层 | MyBatis-Plus 3.5.3.1 |
| 视图层 | Thymeleaf + Bootstrap 5 + jQuery（静态资源全部本地化，无外网 CDN 依赖） |
| 密码 | BCrypt（spring-security-crypto） |
| Excel 解析 | Apache POI 4.1.2 |

## 环境要求

- JDK 1.8（或更高版本 JDK：本工程以 `--release 8` 编译，可在 JDK 8~17 上构建运行）
- Maven 3.3.9（建议同时配置阿里云镜像，见下）
- MySQL 8.0（本机已有服务可直接使用）

## 快速开始

1. **配置数据库**：编辑 `src/main/resources/application.yml` 中的数据源账号密码（默认 `root/123456`，请按本机实际情况修改；也可用环境变量 `DB_USERNAME`/`DB_PASSWORD` 覆盖）。数据库 `mentor_pair` 无需手工创建。
2. **建表与种子数据**：首次启动自动执行 `src/main/resources/db/schema.sql` 与 `db/data.sql`（幂等，可重复执行；也可用 Navicat 11 手动执行）。
3. **构建**：

   ```bash
   mvn clean package -DskipTests
   ```

4. **启动**：

   ```bash
   java -jar target/mentor-pair-1.0.0.jar
   # 或开发模式：mvn spring-boot:run
   ```

5. **访问**：浏览器（360/Chrome/Edge 等 Chromium 内核）打开 <http://localhost:8080/login>

> Maven 阿里云镜像（可选，网络不佳时建议配置 `~/.m2/settings.xml`）：
>
> ```xml
> <settings>
>   <mirrors>
>     <mirror>
>       <id>aliyunmaven</id>
>       <mirrorOf>central</mirrorOf>
>       <url>https://maven.aliyun.com/repository/public</url>
>     </mirror>
>   </mirrors>
> </settings>
> ```

## EXE 版本使用说明（免装 Java）

已用 JDK 17 的 `jpackage` 将系统打成**自带 JRE 的 Windows 程序**，位于 `dist/`：

```
dist/师生导师双选系统/            ← 程序目录（整个文件夹拷贝即可分发）
├─ 师生导师双选系统.exe           ← 双击启动（弹出控制台显示日志，启动后自动打开登录页）
├─ application.yml                ← 外置配置（改数据库账号/端口后重新双击生效）
├─ app/                           ← jar 与外置配置副本
└─ runtime/                       ← 自带 JRE（目标机器无需安装任何 Java）
dist/师生导师双选系统-win64.zip   ← 分发包（解压即用）
```

- **启动**：双击 `师生导师双选系统.exe`，约 10 秒后自动打开浏览器登录页（关闭控制台窗口即停止服务）。
- **三端一体登录**：登录页点击角色卡片选择"管理员端 / 导师端 / 学员端"（导航栏与按钮随角色变色），账号与所选端必须匹配。
- **用完即退**：登录后点击导航栏右侧红色 **退出系统** 按钮，服务优雅关闭、exe 控制台随之退出；直接关闭控制台窗口也可。
- **默认端口 8081**：写在 exe 的启动参数与外置 `application.yml` 中（示例环境 8080 已被占用，故用 8081）。若要改回 8080，编辑 `application.yml` 的 `server.port` 并同步修改 `app/师生导师双选系统.cfg` 中 `[JavaOptions]` 的 `-Dserver.port`。
- **数据库要求**：目标机器需可访问 MySQL 8.0（连接与建库自动完成）。改数据库账号/密码只需编辑 `application.yml`，无需重新打包。
- **重新打包**：`mvn clean package -DskipTests` 后执行
  `"C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\jpackage.exe" --type app-image --name 师生导师双选系统 --input dist-stage --main-jar mentor-pair-1.0.0.jar --main-class org.springframework.boot.loader.JarLauncher --java-options "-Dfile.encoding=UTF-8 -Dserver.port=8081" --win-console --dest dist`
- **关闭浏览器自动打开**：外置 `application.yml` 中 `app.auto-open-browser` 改为 `false`。

## 演示账号

| 角色 | 登录名 | 密码 | 说明 |
|---|---|---|---|
| 管理员 | admin | admin123 | 账号管理、项目/提交/指导信息管理 |
| 导师 | T01 / T02 | 123456 | 名额各 2，各带 1 个开放项目；T01 名下有课程"Java 程序设计"及 5 条成绩 |
| 学员 | S01–S04 | 123456 | 学号即登录名 |

## 功能清单

**管理员**：数据总览（8 项实时统计 + 最近申请）；**双选时间设置（启用后学员仅可在窗口内申请，默认不限）**；学员/导师账号管理（分页搜索、新增、编辑、启停、重置密码，新增导师须填名额；有未完结双选关系的账号不可停用）；项目信息（查看/下架/删除）；项目提交（查看/下载/删除）；指导信息（查看/删除）。

**导师**：资料维护（含可带学员名额）；项目发布/编辑/下架；双选申请处理（接受需校验学员唯一性与名额，事务防并发超收；可查看申请学员已导入成绩）；**导出已接收学员名单（xlsx）**；项目提交查看/下载/填写指导；课程成绩单 Excel 导入（表头：学号、姓名、成绩；逐行容错校验）。

**通用**：登录页选择登录端（三端一体）；自助修改密码；退出系统（服务随浏览器操作优雅关闭）；日志落盘 `logs/app.log`。

**学员**：浏览导师（含剩余名额）与开放项目；提交双选申请（每人同时仅一条待处理/已接受申请；被拒后可重新申请）；上传已完成项目文件（rar/zip/doc/docx/pdf/ppt/pptx，≤20MB，随机重命名存储）；查看导师指导。

## 目录结构

```
mentor-pair/
├─ pom.xml
└─ src/main/
   ├─ java/com/mentorpair/
   │  ├─ MentorPairApplication.java
   │  ├─ common/        # Result、PageVO、BusinessException、GlobalExceptionHandler
   │  ├─ config/        # WebConfig(拦截器)、MybatisPlusConfig(分页/BCrypt)
   │  ├─ interceptor/   # LoginInterceptor、RoleInterceptor
   │  ├─ controller/    # Login、Admin(Page/Api)、Mentor(Page/Api)、Student(Page/Api)、File
   │  ├─ service/ (+impl/)
   │  ├─ mapper/
   │  ├─ entity/
   │  ├─ dto/           # LoginUser、各列表 VO
   │  └─ util/          # ExcelUtil（POI 解析）、TextUtil
   └─ resources/
      ├─ application.yml
      ├─ db/schema.sql、db/data.sql
      ├─ templates/     # login、error/403、admin/、mentor/、student/、fragments/
      └─ static/css|js  # 本地 bootstrap、jquery
```

上传的项目文件存放在运行目录 `./uploads/yyyyMM/` 下（可用 `app.upload-dir` 配置）。

## 常见问题

- **启动报数据库连接失败**：确认 MySQL 服务已启动、`application.yml` 中账号密码正确。
- **重复启动数据会重复吗**：不会。schema/data 脚本均幂等（`IF NOT EXISTS` / `INSERT IGNORE`）。
- **忘记账号密码**：管理员可在"账号管理"中重置任意账号密码为 `123456`。
- **端口冲突**：修改 `application.yml` 中 `server.port`。
