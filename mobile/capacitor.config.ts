import type { CapacitorConfig } from '@capacitor/cli';

/**
 * 部署后请将 server.url 改为手机可访问的地址：
 * - 生产：https://你的域名
 * - 内测：http://192.168.x.x:8080 并设置 cleartext: true（仅 debug）
 */
const BASE_URL = process.env.APP_SERVER_URL || 'http://10.0.2.2:8080';
const USE_CLEARTEXT = BASE_URL.startsWith('http://');

const config: CapacitorConfig = {
  appId: 'com.studycommunity.app',
  appName: '班级学习社区',
  webDir: 'www',
  server: {
    url: BASE_URL,
    cleartext: USE_CLEARTEXT,
    androidScheme: USE_CLEARTEXT ? 'http' : 'https',
  },
  android: {
    allowMixedContent: false,
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      backgroundColor: '#0088ff',
      showSpinner: true,
      spinnerColor: '#ffffff',
    },
  },
};

export default config;
