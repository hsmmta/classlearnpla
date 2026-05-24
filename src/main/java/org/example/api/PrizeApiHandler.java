package org.example.api;

import org.example.util.DBUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PrizeApiHandler {

    void handle(String path, String method, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("/points".equals(path) && "GET".equals(method)) {
            if (!SessionHelper.isLoggedIn(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
                return;
            }
            getPoints(request, response);
            return;
        }
        if ("/points/history".equals(path) && "GET".equals(method)) {
            if (!SessionHelper.isLoggedIn(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
                return;
            }
            history(request, response);
            return;
        }
        if ("/prizes".equals(path) && "GET".equals(method)) {
            if (!SessionHelper.isLoggedIn(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
                return;
            }
            listPrizes(request, response);
            return;
        }
        if ("/prizes/exchange".equals(path) && "POST".equals(method)) {
            if (!SessionHelper.isLoggedIn(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
                return;
            }
            exchange(request, response);
            return;
        }
        JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
    }

    private void getPoints(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        int points = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) points = rs.getInt("points");
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(Map.of("points", points)));
    }

    private void history(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = 10;
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> exchangeRequests = new ArrayList<>();
        int total = 0;
        try {
            ensureExchangeRequestTable(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM pointop WHERE userID = ?")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT opID, pointOP, detail, `time` FROM pointop WHERE userID = ? ORDER BY `time` DESC LIMIT ? OFFSET ?")) {
                ps.setString(1, userID);
                ps.setInt(2, pageSize);
                ps.setInt(3, (page - 1) * pageSize);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("opID", rs.getInt("opID"));
                    row.put("pointOP", rs.getString("pointOP"));
                    row.put("detail", rs.getString("detail"));
                    row.put("time", rs.getTimestamp("time"));
                    list.add(row);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT requestID, goodsID, goodsName, goodsType, needPoints, status, createdAt, processedAt, remark " +
                            "FROM exchange_request WHERE userID = ? ORDER BY createdAt DESC LIMIT 50")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("requestID", rs.getLong("requestID"));
                    row.put("goodsID", rs.getString("goodsID"));
                    row.put("goodsName", rs.getString("goodsName"));
                    row.put("goodsType", rs.getString("goodsType"));
                    row.put("needPoints", rs.getInt("needPoints"));
                    row.put("status", rs.getString("status"));
                    row.put("createdAt", rs.getTimestamp("createdAt"));
                    row.put("processedAt", rs.getTimestamp("processedAt"));
                    row.put("remark", rs.getString("remark"));
                    exchangeRequests.add(row);
                }
            }
        } finally {
            conn.close();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("exchangeRequests", exchangeRequests);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        JsonResponse.write(response, JsonResponse.ok(data));
    }

    private void listPrizes(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int page = parseInt(request.getParameter("page"), 1);
        int pageSize = 9;
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        int userPoints = 0;
        List<Map<String, Object>> goods = new ArrayList<>();
        int total = 0;
        try {
            ensurePrizeColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) userPoints = rs.getInt("points");
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM goodslist WHERE enabled = 1")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT goodsID, goodsName, goodsType, needPoints, currentNum FROM goodslist WHERE enabled = 1 LIMIT ? OFFSET ?")) {
                ps.setInt(1, pageSize);
                ps.setInt(2, (page - 1) * pageSize);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> g = new HashMap<>();
                    g.put("goodsID", rs.getString("goodsID"));
                    g.put("goodsName", rs.getString("goodsName"));
                    g.put("goodsType", rs.getString("goodsType"));
                    g.put("needPoints", rs.getInt("needPoints"));
                    g.put("currentNum", rs.getInt("currentNum"));
                    goods.add(g);
                }
            }
        } finally {
            conn.close();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userPoints", userPoints);
        data.put("goods", goods);
        data.put("total", total);
        data.put("page", page);
        JsonResponse.write(response, JsonResponse.ok(data));
    }

    private void exchange(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        String goodsID = request.getParameter("itemID");
        if (goodsID == null) goodsID = request.getParameter("goodsID");
        if (goodsID == null || goodsID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("商品参数错误"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensurePrizeColumns(conn);
            ensureExchangeRequestTable(conn);

            String goodsName = null;
            String goodsType = null;
            int needPoints = 0;
            int currentNum = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT goodsName, goodsType, needPoints, currentNum, enabled FROM goodslist WHERE goodsID = ? FOR UPDATE")) {
                ps.setString(1, goodsID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("商品不存在"));
                    return;
                }
                if (rs.getInt("enabled") != 1) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("该商品已下架"));
                    return;
                }
                goodsName = rs.getString("goodsName");
                goodsType = rs.getString("goodsType");
                needPoints = rs.getInt("needPoints");
                currentNum = rs.getInt("currentNum");
            }

            int current = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ? FOR UPDATE")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("用户积分不存在"));
                    return;
                }
                current = rs.getInt("points");
            }
            if (current < needPoints) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("积分不足"));
                return;
            }
            boolean physical = "实体奖品".equals(goodsType);
            if (physical && currentNum <= 0) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("该实体奖品已兑换完"));
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = points - ? WHERE userID = ?")) {
                ps.setInt(1, needPoints);
                ps.setString(2, userID);
                ps.executeUpdate();
            }
            if (physical) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE goodslist SET currentNum = currentNum - 1 WHERE goodsID = ? AND currentNum > 0")) {
                    ps.setString(1, goodsID);
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        conn.rollback();
                        JsonResponse.write(response, JsonResponse.fail("该实体奖品已兑换完"));
                        return;
                    }
                }
            }

            if (physical) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO exchange_request (userID, goodsID, goodsName, goodsType, needPoints, status, createdAt) " +
                                "VALUES (?, ?, ?, ?, ?, 'pending', ?)")) {
                    ps.setString(1, userID);
                    ps.setString(2, goodsID);
                    ps.setString(3, goodsName == null ? goodsID : goodsName);
                    ps.setString(4, goodsType);
                    ps.setInt(5, needPoints);
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, userID);
                ps.setString(2, "-" + needPoints);
                if (physical) {
                    ps.setString(3, "兑换实体奖品申请：" + (goodsName == null ? goodsID : goodsName) + "（待管理员处理）");
                } else {
                    ps.setString(3, "兑换虚拟奖品：" + (goodsName == null ? goodsID : goodsName));
                }
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
            conn.commit();
            if (physical) JsonResponse.write(response, JsonResponse.ok("兑换申请已提交，等待管理员处理"));
            else JsonResponse.write(response, JsonResponse.ok("兑换成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("兑换失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void ensurePrizeColumns(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "ALTER TABLE goodslist ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void ensureExchangeRequestTable(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS exchange_request (" +
                        "requestID BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "userID VARCHAR(64) NOT NULL," +
                        "goodsID VARCHAR(64) NOT NULL," +
                        "goodsName VARCHAR(255) NOT NULL," +
                        "goodsType VARCHAR(32) NOT NULL," +
                        "needPoints INT NOT NULL," +
                        "status VARCHAR(16) NOT NULL DEFAULT 'pending'," +
                        "createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "processedAt TIMESTAMP NULL," +
                        "processedBy VARCHAR(64) NULL," +
                        "remark VARCHAR(255) NULL" +
                        ")")) {
            ps.executeUpdate();
        }
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
