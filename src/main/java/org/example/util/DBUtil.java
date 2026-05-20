package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库工具类。连接信息从环境变量 / .env 读取（见 .env.example）。
 */
public class DBUtil {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/web_demo?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        String url = EnvUtil.get("DB_URL", DEFAULT_URL);
        String username = EnvUtil.get("DB_USERNAME", "root");
        String password = EnvUtil.get("DB_PASSWORD");
        if (password.isEmpty()) {
            System.err.println("数据库连接失败：未设置 DB_PASSWORD，请配置 .env 或环境变量");
            return null;
        }
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("数据库连接失败：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}
