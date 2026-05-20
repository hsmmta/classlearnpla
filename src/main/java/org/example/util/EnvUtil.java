package org.example.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 从系统环境变量、JVM 属性或项目根目录 .env 文件读取配置（不提交 .env 到 Git）。
 */
public final class EnvUtil {
    private static final Map<String, String> DOTENV;

    static {
        DOTENV = loadDotEnv();
    }

    private EnvUtil() {
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {
        String v = System.getenv(key);
        if (isNotBlank(v)) {
            return v.trim();
        }
        v = System.getProperty(key);
        if (isNotBlank(v)) {
            return v.trim();
        }
        v = DOTENV.get(key);
        if (isNotBlank(v)) {
            return v.trim();
        }
        return defaultValue == null ? "" : defaultValue;
    }

    public static String require(String key) {
        String v = get(key);
        if (!isNotBlank(v)) {
            throw new IllegalStateException(
                    "缺少配置项 " + key + "，请在 .env 或环境变量中设置（参考 .env.example）");
        }
        return v;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static Map<String, String> loadDotEnv() {
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).resolve(".env");
        Path catalinaBase = System.getProperty("catalina.base") != null
                ? Paths.get(System.getProperty("catalina.base")).resolve(".env")
                : null;

        Map<String, String> merged = new HashMap<>();
        if (catalinaBase != null) {
            merged.putAll(parseDotEnvFile(catalinaBase));
        }
        merged.putAll(parseDotEnvFile(userDir));
        return Collections.unmodifiableMap(merged);
    }

    private static Map<String, String> parseDotEnvFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("[EnvUtil] 读取 .env 失败: " + path + " — " + e.getMessage());
        }
        return map;
    }
}
