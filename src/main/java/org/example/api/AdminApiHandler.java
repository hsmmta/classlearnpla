package org.example.api;

import org.example.util.DBUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class AdminApiHandler {

    void handle(String path, String method, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!SessionHelper.isAdmin(request)) {
            JsonResponse.write(response, HttpServletResponse.SC_FORBIDDEN, JsonResponse.fail("需要管理员权限"));
            return;
        }
        if ("/admin/prizes".equals(path)) {
            if ("GET".equals(method)) listPrizes(response);
            else if ("POST".equals(method)) addPrize(request, response);
            else if ("DELETE".equals(method)) deletePrize(request, response);
            else methodNotAllowed(response);
            return;
        }
        if ("/admin/points".equals(path)) {
            if ("GET".equals(method)) queryPoints(request, response);
            else if ("POST".equals(method)) modifyPoints(request, response);
            else methodNotAllowed(response);
            return;
        }
        if ("/admin/users/penalty".equals(path) && "GET".equals(method)) {
            queryPenalty(request, response);
            return;
        }
        if ("/admin/users/ban".equals(path) && "POST".equals(method)) {
            banUser(request, response);
            return;
        }
        if ("/admin/users/unban".equals(path) && "POST".equals(method)) {
            unbanUser(request, response);
            return;
        }
        if ("/admin/goods".equals(path) && "POST".equals(method)) {
            addGoods(request, response);
            return;
        }
        JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
    }

    private void methodNotAllowed(HttpServletResponse response) throws IOException {
        JsonResponse.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, JsonResponse.fail("方法不允许"));
    }

    private void listPrizes(HttpServletResponse response) throws SQLException, IOException {
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT goodsID, goodsName, goodsType, needPoints, currentNum FROM goodslist");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> g = new HashMap<>();
                g.put("goodsID", rs.getString("goodsID"));
                g.put("goodsName", rs.getString("goodsName"));
                g.put("goodsType", rs.getString("goodsType"));
                g.put("needPoints", rs.getInt("needPoints"));
                g.put("currentNum", rs.getInt("currentNum"));
                list.add(g);
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void addPrize(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String goodsID = request.getParameter("goodsID");
        String goodsName = request.getParameter("goodsName");
        String goodsType = request.getParameter("goodsType");
        int needPoints = Integer.parseInt(request.getParameter("needPoints"));
        int currentNum = Integer.parseInt(request.getParameter("currentNum"));
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO goodslist (goodsID, goodsName, goodsType, needPoints, currentNum) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, goodsID);
            ps.setString(2, goodsName);
            ps.setString(3, goodsType);
            ps.setInt(4, needPoints);
            ps.setInt(5, currentNum);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                JsonResponse.write(response, JsonResponse.fail("商品ID已存在"));
                return;
            }
            throw e;
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("上架成功"));
    }

    private void deletePrize(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String goodsID = request.getParameter("goodsID");
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM goodslist WHERE goodsID = ?")) {
            ps.setString(1, goodsID);
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("已删除"));
    }

    private void addGoods(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        addPrize(request, response);
    }

    private void queryPoints(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        Connection conn = DBUtil.getConnection();
        int points = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) points = rs.getInt("points");
            else {
                JsonResponse.write(response, JsonResponse.fail("用户不存在或未初始化积分"));
                return;
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(Map.of("points", points)));
    }

    private void modifyPoints(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        String operation = request.getParameter("operation");
        int amount = Integer.parseInt(request.getParameter("pointAmount"));
        String reason = request.getParameter("reason");
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            int current = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("用户不存在"));
                    return;
                }
                current = rs.getInt("points");
            }
            int delta = amount;
            if ("subtract".equals(operation)) delta = -amount;
            else if ("set".equals(operation)) delta = amount - current;
            int newPoints = current + delta;
            if ("set".equals(operation)) newPoints = amount;
            try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = ? WHERE userID = ?")) {
                ps.setInt(1, newPoints);
                ps.setString(2, userID);
                ps.executeUpdate();
            }
            String opStr = delta >= 0 ? "+" + delta : String.valueOf(delta);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, userID);
                ps.setString(2, opStr);
                ps.setString(3, reason == null ? "管理员调整" : reason);
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok(Map.of("points", newPoints)));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("修改失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void queryPenalty(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try {
            Map<String, Object> data = UserGovernance.queryPenalty(conn, userID.trim());
            JsonResponse.write(response, JsonResponse.ok(data));
        } finally {
            conn.close();
        }
    }

    private void banUser(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        int days = parseInt(request.getParameter("days"), 3);
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try {
            UserGovernance.banUser(conn, userID.trim(), Math.max(days, 1));
            JsonResponse.write(response, JsonResponse.ok("封禁成功"));
        } finally {
            conn.close();
        }
    }

    private void unbanUser(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try {
            UserGovernance.unbanUser(conn, userID.trim());
            JsonResponse.write(response, JsonResponse.ok("解封成功"));
        } finally {
            conn.close();
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
