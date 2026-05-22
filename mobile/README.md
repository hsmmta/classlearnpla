# 班级学习社区 — Android App（Capacitor）

## 配置服务器地址

编辑 [capacitor.config.ts](./capacitor.config.ts) 中的 `BASE_URL`，或构建前设置环境变量：

```bash
# Windows PowerShell（内测局域网示例）
$env:APP_SERVER_URL="http://192.168.1.100:8080"
npm run cap:sync

# 生产 HTTPS
$env:APP_SERVER_URL="https://your-domain.example.com"
npm run cap:sync
```

默认 `http://10.0.2.2:8080` 供 **Android 模拟器** 访问本机 Tomcat（`mvn cargo:run` 或本地 8080）。

真机内测请改为你电脑的局域网 IP，并在 [android/app/src/main/AndroidManifest.xml](./android/app/src/main/AndroidManifest.xml) 中保持 `usesCleartextTraffic="true"`（仅 debug）。

## 首次构建

```bash
cd mobile
npm install
npx cap add android
npx cap sync android
npx cap open android
```

在 Android Studio 中：**Run** 生成 debug APK，或 **Build → Generate Signed Bundle/APK** 生成 release。

命令行 debug 包（需已安装 Android SDK）：

```bash
cd android
gradlew.bat assembleDebug
# 输出: android/app/build/outputs/apk/debug/app-debug.apk
```

## 插件说明

- `@capacitor/app`：Android 返回键在 WebView 历史内后退
- `@capacitor/splash-screen` / `status-bar`：启动屏与状态栏

完整部署见 [说明/Android-App部署.md](../说明/Android-App部署.md)。
