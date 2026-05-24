import type { CapacitorConfig } from '@capacitor/cli';

/**
 * Remote WebView mode:
 * - Emulator: http://10.0.2.2:8080
 * - Real device: http://<LAN-IP>:8080
 * - Production: https://<domain>
 */
const BASE_URL = process.env.APP_SERVER_URL || 'http://10.0.2.2:8080';
const FORCE_CLEAR_TEXT = process.env.APP_ALLOW_CLEARTEXT === 'true';
const USE_CLEARTEXT = BASE_URL.startsWith('http://') || FORCE_CLEAR_TEXT;

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
