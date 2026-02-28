package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库工具类：封装连接、关闭数据库的方法
 */
public class DBUtil {
    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/web_demo?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    // 加载驱动（MySQL 8.x无需手动加载，此处为兼容写法）
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


     //获取数据库连接
     public static Connection getConnection() {
         Connection conn = null;
         try {
             conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             System.out.println("数据库连接成功！");
         } catch (SQLException e) {
             System.out.println("数据库连接失败！异常信息：" + e.getMessage());
             e.printStackTrace();
         }
         return conn;
     }
     //关闭数据库资源

    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 重载：关闭连接和PreparedStatement（无ResultSet时用）
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}
