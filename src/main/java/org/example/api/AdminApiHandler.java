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
        if ("/admin/prizes/stock".equals(path) && "POST".equals(method)) {
            adjustPrizeStock(request, response);
            return;
        }
        if ("/admin/prizes/toggle".equals(path) && "POST".equals(method)) {
            togglePrize(request, response);
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
        if ("/admin/users/penalty-records".equals(path) && "GET".equals(method)) {
            queryPenaltyRecords(request, response);
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
        if ("/admin/users/warn".equals(path) && "POST".equals(method)) {
            warnUser(request, response);
            return;
        }
        if ("/admin/exchange-requests".equals(path) && "GET".equals(method)) {
            listExchangeRequests(request, response);
            return;
        }
        if ("/admin/exchange-requests/process".equals(path) && "POST".equals(method)) {
            processExchangeRequest(request, response);
            return;
        }
        if ("/admin/users/profile".equals(path)) {
            if ("GET".equals(method)) queryUserProfile(request, response);
            else if ("POST".equals(method)) updateUserProfile(request, response);
            else methodNotAllowed(response);
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
        try {
            ensurePrizeColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT goodsID, goodsName, goodsType, needPoints, currentNum, enabled FROM goodslist");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> g = new HashMap<>();
                g.put("goodsID", rs.getString("goodsID"));
                g.put("goodsName", rs.getString("goodsName"));
                g.put("goodsType", rs.getString("goodsType"));
                g.put("needPoints", rs.getInt("needPoints"));
                g.put("currentNum", rs.getInt("currentNum"));
                g.put("enabled", rs.getInt("enabled"));
                list.add(g);
            }
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
        try {
            ensurePrizeColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO goodslist (goodsID, goodsName, goodsType, needPoints, currentNum) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, goodsID);
            ps.setString(2, goodsName);
            ps.setString(3, goodsType);
            ps.setInt(4, needPoints);
            ps.setInt(5, currentNum);
            ps.executeUpdate();
            }
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

    private void adjustPrizeStock(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String goodsID = request.getParameter("goodsID");
        int delta = parseInt(request.getParameter("delta"), 0);
        if (goodsID == null || goodsID.trim().isEmpty() || delta == 0) {
            JsonResponse.write(response, JsonResponse.fail("参数错误"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try {
            ensurePrizeColumns(conn);
            String goodsType = null;
            int currentNum = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT goodsType, currentNum FROM goodslist WHERE goodsID = ?")) {
                ps.setString(1, goodsID.trim());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    JsonResponse.write(response, JsonResponse.fail("商品不存在"));
                    return;
                }
                goodsType = rs.getString("goodsType");
                currentNum = rs.getInt("currentNum");
            }
            if (!"实体奖品".equals(goodsType)) {
                JsonResponse.write(response, JsonResponse.fail("仅实体奖品可调整库存"));
                return;
            }
            int target = Math.max(0, currentNum + delta);
            try (PreparedStatement ps = conn.prepareStatement("UPDATE goodslist SET currentNum = ? WHERE goodsID = ?")) {
                ps.setInt(1, target);
                ps.setString(2, goodsID.trim());
                ps.executeUpdate();
            }
            JsonResponse.write(response, JsonResponse.ok(Map.of("currentNum", target)));
        } finally {
            conn.close();
        }
    }

    private void togglePrize(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String goodsID = request.getParameter("goodsID");
        int enabled = parseInt(request.getParameter("enabled"), -1);
        if (goodsID == null || goodsID.trim().isEmpty() || (enabled != 0 && enabled != 1)) {
            JsonResponse.write(response, JsonResponse.fail("参数错误"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try {
            ensurePrizeColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement("UPDATE goodslist SET enabled = ? WHERE goodsID = ?")) {
                ps.setInt(1, enabled);
                ps.setString(2, goodsID.trim());
                int n = ps.executeUpdate();
                if (n == 0) {
                    JsonResponse.write(response, JsonResponse.fail("商品不存在"));
                    return;
                }
            }
            JsonResponse.write(response, JsonResponse.ok(enabled == 1 ? "已上架" : "已下架"));
        } finally {
            conn.close();
        }
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
            String uid = userID.trim();
            int banDays = Math.max(days, 1);
            UserGovernance.banUser(conn, uid, banDays);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '0', ?, ?)")) {
                ps.setString(1, uid);
                ps.setString(2, "管理员手动封禁 " + banDays + " 天");
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
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
            String uid = userID.trim();
            UserGovernance.unbanUser(conn, uid);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '0', '管理员手动解封', ?)")) {
                ps.setString(1, uid);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            JsonResponse.write(response, JsonResponse.ok("解封成功"));
        } finally {
            conn.close();
        }
    }

    private void warnUser(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        String reason = request.getParameter("reason");
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        String uid = userID.trim();
        Connection conn = DBUtil.getConnection();
        try {
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM user WHERE userphone = ?")) {
                ps.setString(1, uid);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    JsonResponse.write(response, JsonResponse.fail("用户不存在"));
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM admin WHERE adminID = ?")) {
                ps.setString(1, uid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    JsonResponse.write(response, JsonResponse.fail("不能警告管理员账号"));
                    return;
                }
            }

            int warningCount = UserGovernance.increaseWarningAndMaybeBan(conn, uid);
            String detail = "管理员警告："
                    + (reason == null || reason.trim().isEmpty() ? "评论区不当言论" : reason.trim())
                    + "；当前警告次数：" + warningCount;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '0', ?, ?)")) {
                ps.setString(1, uid);
                ps.setString(2, detail);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }

            Map<String, Object> penalty = UserGovernance.queryPenalty(conn, uid);
            JsonResponse.write(response, JsonResponse.ok(penalty));
        } catch (Exception e) {
            e.printStackTrace();
            JsonResponse.write(response, JsonResponse.fail("警告失败：" + e.getMessage()));
        } finally {
            conn.close();
        }
    }

    private void queryPenaltyRecords(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        int limit = parseInt(request.getParameter("limit"), 20);
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT pointOP, detail, `time` FROM pointop " +
                        "WHERE userID = ? AND (" +
                        "pointOP = '-30' OR detail LIKE '%警告%' OR detail LIKE '%封禁%' OR detail LIKE '%惩罚%' OR detail LIKE '%处罚%'" +
                        ") ORDER BY `time` DESC LIMIT ?")) {
            ps.setString(1, userID.trim());
            ps.setInt(2, safeLimit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("pointOP", rs.getString("pointOP"));
                row.put("detail", rs.getString("detail"));
                row.put("time", rs.getTimestamp("time"));
                list.add(row);
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void listExchangeRequests(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int limit = parseInt(request.getParameter("limit"), 200);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        Connection conn = DBUtil.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ensureExchangeRequestTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT r.requestID, r.userID, r.goodsID, r.goodsName, r.goodsType, r.needPoints, r.status, " +
                            "r.createdAt, r.processedAt, r.processedBy, r.remark, u.userName " +
                            "FROM exchange_request r LEFT JOIN user u ON r.userID = u.userphone " +
                            "ORDER BY CASE WHEN r.status = 'pending' THEN 0 ELSE 1 END, r.createdAt DESC LIMIT ?")) {
                ps.setInt(1, safeLimit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("requestID", rs.getLong("requestID"));
                    row.put("userID", rs.getString("userID"));
                    row.put("userName", rs.getString("userName"));
                    row.put("goodsID", rs.getString("goodsID"));
                    row.put("goodsName", rs.getString("goodsName"));
                    row.put("goodsType", rs.getString("goodsType"));
                    row.put("needPoints", rs.getInt("needPoints"));
                    row.put("status", rs.getString("status"));
                    row.put("createdAt", rs.getTimestamp("createdAt"));
                    row.put("processedAt", rs.getTimestamp("processedAt"));
                    row.put("processedBy", rs.getString("processedBy"));
                    row.put("remark", rs.getString("remark"));
                    list.add(row);
                }
            }
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok(list));
    }

    private void processExchangeRequest(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        long requestID = parseLong(request.getParameter("requestID"), 0L);
        String action = request.getParameter("action");
        String remark = request.getParameter("remark");
        if (requestID <= 0 || action == null) {
            JsonResponse.write(response, JsonResponse.fail("参数错误"));
            return;
        }
        String nextStatus;
        if ("fulfilled".equals(action)) nextStatus = "fulfilled";
        else if ("rejected".equals(action)) nextStatus = "rejected";
        else {
            JsonResponse.write(response, JsonResponse.fail("处理动作不支持"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensureExchangeRequestTable(conn);
            String userID = null;
            String goodsName = null;
            String curStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT userID, goodsName, status FROM exchange_request WHERE requestID = ? FOR UPDATE")) {
                ps.setLong(1, requestID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("申请不存在"));
                    return;
                }
                userID = rs.getString("userID");
                goodsName = rs.getString("goodsName");
                curStatus = rs.getString("status");
            }
            if (!"pending".equalsIgnoreCase(curStatus)) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("该申请已处理，不能重复处理"));
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE exchange_request SET status = ?, processedAt = ?, processedBy = ?, remark = ? WHERE requestID = ? AND status = 'pending'")) {
                ps.setString(1, nextStatus);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, SessionHelper.getUserId(request));
                ps.setString(4, remark == null ? "" : remark.trim());
                ps.setLong(5, requestID);
                int n = ps.executeUpdate();
                if (n == 0) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("该申请已处理，不能重复处理"));
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '0', ?, ?)")) {
                ps.setString(1, userID);
                String detail = "实体奖品申请「" + (goodsName == null ? "" : goodsName) + "」"
                        + ("fulfilled".equals(nextStatus) ? "已兑换完成" : "已拒绝兑换");
                if (remark != null && !remark.trim().isEmpty()) {
                    detail = detail + "（" + remark.trim() + "）";
                }
                ps.setString(2, detail);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("处理成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("处理失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void queryUserProfile(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        String uid = userID.trim();
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT userphone, userName, classID, gender, studentID, userEmail, userStatus FROM user WHERE userphone = ?")) {
            ps.setString(1, uid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                JsonResponse.write(response, JsonResponse.fail("用户不存在"));
                return;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("userID", rs.getString("userphone"));
            data.put("userName", rs.getString("userName"));
            data.put("classID", rs.getString("classID"));
            data.put("gender", rs.getString("gender"));
            data.put("studentID", rs.getString("studentID"));
            data.put("userEmail", rs.getString("userEmail"));
            data.put("userStatus", rs.getInt("userStatus"));
            JsonResponse.write(response, JsonResponse.ok(data));
        } finally {
            conn.close();
        }
    }

    private void updateUserProfile(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        if (userID == null || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请提供用户手机号"));
            return;
        }
        String uid = userID.trim();
        String userName = trimParam(request.getParameter("userName"));
        String classID = trimOrEmpty(request.getParameter("classID"));
        String gender = trimOrEmpty(request.getParameter("gender"));
        String studentID = trimOrEmpty(request.getParameter("studentID"));
        String userEmail = trimOrEmpty(request.getParameter("userEmail"));
        if (userName == null || userName.isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("昵称不能为空"));
            return;
        }

        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE user SET userName=?, classID=?, gender=?, studentID=?, userEmail=? WHERE userphone=?")) {
                ps.setString(1, userName);
                ps.setString(2, classID);
                ps.setString(3, gender);
                ps.setString(4, studentID);
                ps.setString(5, userEmail);
                ps.setString(6, uid);
                int n = ps.executeUpdate();
                if (n == 0) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("用户不存在"));
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE points SET userName = ? WHERE userID = ?")) {
                ps.setString(1, userName);
                ps.setString(2, uid);
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("资料更新成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("更新失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private String trimParam(String value) {
        return value == null ? null : value.trim();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
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

    private long parseLong(String s, long def) {
        if (s == null) return def;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
