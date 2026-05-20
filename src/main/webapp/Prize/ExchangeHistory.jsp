<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.example.util.DBUtil" %>
<%
    // 检查用户是否已登录
    String userID = (String) session.getAttribute("userID");
    if (userID == null || userID.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/auth/signin.html");
        return;
    }

    String userName = (String) session.getAttribute("userName");
    int userPoints = 0;

    // 查询用户当前积分
    try {
        Connection conn = DBUtil.getConnection();
        if (conn == null) throw new SQLException("数据库未连接");
        String sql = "SELECT points FROM points WHERE userID = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userID);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            userPoints = rs.getInt("points");
        }
        rs.close();
        pstmt.close();
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

    // 查询兑换历史
    List<Map<String, Object>> history = new ArrayList<Map<String, Object>>();
    try {
        Connection conn = DBUtil.getConnection();
        if (conn == null) throw new SQLException("数据库未连接");
        String sql = "SELECT opID, pointOP, detail, `time` FROM pointop WHERE userID = ? ORDER BY `time` DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userID);
        ResultSet rs = pstmt.executeQuery();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (rs.next()) {
            Map<String, Object> record = new HashMap<String, Object>();
            record.put("opID", rs.getInt("opID"));
            record.put("pointOP", rs.getString("pointOP"));
            record.put("detail", rs.getString("detail"));
            record.put("time", sdf.format(rs.getTimestamp("time")));
            history.add(record);
        }

        rs.close();
        pstmt.close();
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>兑换历史</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: "Microsoft YaHei", sans-serif;
        }
        body {
            background-color: #f5f5f5;
        }
        .nav-bar {
            background-color: white;
            padding: 15px 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            margin-bottom: 30px;
        }
        .nav-bar ul {
            list-style: none;
            display: flex;
            justify-content: center;
            gap: 40px;
            flex-wrap: wrap;
        }
        .nav-bar a {
            text-decoration: none;
            color: #333;
            font-size: 16px;
            transition: color 0.3s;
        }
        .nav-bar a:hover, .nav-bar .active {
            color: #0088ff;
            font-weight: bold;
        }
        .user-info-bar {
            background-color: white;
            padding: 15px 20px;
            text-align: right;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            margin-bottom: 20px;
        }
        .user-info-bar span {
            margin: 0 20px;
            font-size: 14px;
            color: #666;
        }
        .user-info-bar .points {
            color: #ff9800;
            font-weight: bold;
            font-size: 16px;
        }
        .container {
            max-width: 1000px;
            margin: 0 auto;
            padding: 0 20px;
        }
        .page-title {
            font-size: 24px;
            font-weight: bold;
            color: #333;
            margin-bottom: 30px;
            text-align: center;
        }
        .back-btn {
            display: inline-block;
            margin-bottom: 20px;
            padding: 8px 16px;
            background-color: #0088ff;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            transition: background-color 0.3s;
        }
        .back-btn:hover {
            background-color: #0077ee;
        }
        .history-table {
            background-color: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th {
            background-color: #f8f9fa;
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: #333;
            border-bottom: 2px solid #e9ecef;
        }
        td {
            padding: 15px;
            border-bottom: 1px solid #e9ecef;
            color: #666;
        }
        tr:last-child td {
            border-bottom: none;
        }
        tr:hover {
            background-color: #f8f9fa;
        }
        .point-change {
            font-weight: bold;
            font-size: 16px;
        }
        .point-change.positive {
            color: #28a745;
        }
        .point-change.negative {
            color: #dc3545;
        }
        .empty-state {
            text-align: center;
            padding: 50px 20px;
            color: #999;
        }
        .empty-state p {
            font-size: 16px;
            margin: 20px 0;
        }
        @media (max-width: 768px) {
            table {
                font-size: 14px;
            }
            th, td {
                padding: 10px;
            }
        }
    </style>
</head>
<body>
<div class="nav-bar">
    <ul>
        <li><a href="${pageContext.request.contextPath}/home/index.jsp">首页</a></li>
        <li><a href="${pageContext.request.contextPath}/material/search">资料区</a></li>
        <li><a href="${pageContext.request.contextPath}/discussion/list">讨论区</a></li>
        <li><a href="${pageContext.request.contextPath}/Prize/DisplayPrize.jsp" class="active">积分商城</a></li>
        <li><a href="${pageContext.request.contextPath}/personalInfo/getUserInfo.jsp">个人中心</a></li>
        <li><a href="${pageContext.request.contextPath}/auth/signin.html" onclick="return confirm('确定要退出登录吗？')">退出登录</a></li>
    </ul>
</div>

<div class="user-info-bar">
    <span>欢迎，<%= userName %></span>
    <span>当前积分：<span class="points"><%= userPoints %></span></span>
</div>

<div class="container">
    <a href="${pageContext.request.contextPath}/Prize/DisplayPrize.jsp" class="back-btn">返回积分商城</a>

    <h1 class="page-title">兑换历史</h1>

    <% if (history.isEmpty()) { %>
        <div class="empty-state">
            <p>暂无兑换记录</p>
        </div>
    <% } else { %>
        <div class="history-table">
            <table>
                <thead>
                    <tr>
                        <th>编号</th>
                        <th>积分变化</th>
                        <th>详情</th>
                        <th>时间</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> record : history) {
                        String pointOP = (String) record.get("pointOP");
                        boolean isNegative = pointOP.startsWith("-");
                    %>
                        <tr>
                            <td><%= record.get("opID") %></td>
                            <td>
                                <span class="point-change <%= isNegative ? "negative" : "positive" %>">
                                    <%= pointOP %>
                                </span>
                            </td>
                            <td><%= record.get("detail") %></td>
                            <td><%= record.get("time") %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    <% } %>
</div>
</body>
</html>

