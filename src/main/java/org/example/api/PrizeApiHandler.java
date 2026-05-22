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
        int total = 0;
        try {
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
        } finally {
            conn.close();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
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
            try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) userPoints = rs.getInt("points");
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM goodslist")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT goodsID, goodsName, goodsType, needPoints, currentNum FROM goodslist LIMIT ? OFFSET ?")) {
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
        int needPoints = parseInt(request.getParameter("needPoints"), 0);
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
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
            try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = points - ? WHERE userID = ?")) {
                ps.setInt(1, needPoints);
                ps.setString(2, userID);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, userID);
                ps.setString(2, "-" + needPoints);
                ps.setString(3, "兑换" + goodsID);
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("兑换成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("兑换失败"));
        } finally {
            conn.setAutoCommit(true);
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
