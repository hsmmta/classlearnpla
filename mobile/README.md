# 班级学习社区 Android App（Vue + Capacitor）

本工程采用 **Capacitor Android 壳 + 远程加载 Vue SPA** 的方式运行。  
即：App 真正安装在手机上，但页面和 API 仍来自你当前后端服务。

## 1. 前置条件

- JDK 21+
- Node.js 18+（建议 LTS）
- Android Studio（含 Android SDK / Platform Tools）
- 后端可启动并可被手机访问（同一局域网）

> 如果系统默认 Java 版本过高（如 JDK 25）导致 Gradle 报错，请临时指定 Android Studio 自带 JBR：  
> `E:\\android\\Android Studio\\jbr`

## 2. 启动顺序（推荐）

### 2.1 启动后端

在仓库根目录执行：

```powershell
.\scripts\run-dev.cmd
```

如果你只想单独启动后端，也可使用 Maven 方式。

### 2.2 构建前端到 webapp

```powershell
cd frontend
npm install
npm run build
```

### 2.3 同步 Capacitor Android 工程

```powershell
cd ..\mobile
npm install
$env:APP_SERVER_URL="http://10.0.2.2:8080"   # 模拟器默认
$env:JAVA_HOME="E:\android\Android Studio\jbr"
$env:ANDROID_HOME="E:\android\Sdk"
npm run cap:sync
```

## 3. Android Studio 运行方式

### 3.1 打开工程

```powershell
cd mobile
npm run cap:open
```

Android Studio 中选择设备后点击 **Run**。

### 3.2 常见构建命令（命令行）

```powershell
cd mobile
$env:JAVA_HOME="E:\android\Android Studio\jbr"
$env:ANDROID_HOME="E:\android\Sdk"
npm run android:debug   # 构建 debug APK
npm run android:run     # 安装到已连接设备
```

生成 APK 路径：

`mobile/android/app/build/outputs/apk/debug/app-debug.apk`

## 4. 真机调试与安装（重点）

### 4.1 手机准备

- 开启开发者选项
- 开启 USB 调试
- 首次连接电脑时允许调试授权

### 4.2 电脑与手机同一 Wi-Fi

将 `APP_SERVER_URL` 改为你电脑局域网 IP，例如：

```powershell
cd mobile
$env:APP_SERVER_URL="http://192.168.1.100:8080"
$env:APP_ALLOW_CLEARTEXT="true"
npm run cap:sync
npm run android:run
```

> 提示：`APP_ALLOW_CLEARTEXT=true` 仅用于内网 HTTP 调试，正式环境建议 HTTPS。

### 4.3 真机安装 APK（不走 IDE）

构建后将 APK 拷贝到手机并安装：

`mobile/android/app/build/outputs/apk/debug/app-debug.apk`

## 5. 生产发布（简要）

- Android Studio → `Build` → `Generate Signed Bundle / APK`
- 选择 `Android App Bundle (AAB)` 更适合上架
- 使用正式签名证书并切换 HTTPS 服务地址

## 6. 故障排查

- **App 打开白屏/无法连接**
  - 确认后端已启动在 `8080`
  - 确认手机能访问 `http://<电脑IP>:8080`
  - 检查 `APP_SERVER_URL` 是否正确并已重新 `cap:sync`
- **登录态异常**
  - 确认前后端同源访问（App 指向同一后端域名/IP）
  - 清理应用数据后重试
- **上传失败**
  - 检查后端日志与文件大小限制（PDF 30MB / 图片限制）
- **端口冲突**
  - 关闭占用 8080 的进程或修改本地启动端口
- **Gradle 提示 non-ASCII path**
  - 本项目目录含中文，已在 `mobile/android/gradle.properties` 设置 `android.overridePathCheck=true`
  - 若你迁移目录后仍报错，可保留该配置
