# 班级学习社区平台

Java Servlet + MySQL 后端，Vue 3 + Element Plus 前端 SPA。

## 开发环境

1. 复制 `.env.example` 为 `.env` 并填写数据库、短信、邮箱配置
2. 启动 MySQL，导入数据库脚本（见 `说明/` 目录）
3. 启动后端（需 **JDK 17+**，与 `pom.xml` 中 Java 21 一致）：
   - 推荐：`.\scripts\run-dev.cmd`（自动查找本机 JDK；若 `.ps1` 被策略拦截请用此文件）
   - 或：`powershell -ExecutionPolicy Bypass -File .\scripts\run-dev.ps1`
   - 或先设置 `JAVA_HOME` 后执行：`mvn cargo:run`
   - 勿再用 `mvn tomcat7:run`（Tomcat 7 仅支持 Java 8，无法加载本项目 class）
4. 启动前端：

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173 ，API 通过 Vite 代理到 8080。

## 生产构建

```bash
cd frontend && npm run build
mvn clean package
```

前端产物输出到 `src/main/webapp/`，与 WAR 一并部署。

## 技术栈

- 后端：Java 21、Servlet 4、Gson、MySQL
- 前端：Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios
- 移动端：Capacitor 壳 App（见 `mobile/README.md`）
