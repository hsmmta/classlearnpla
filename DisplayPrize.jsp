<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%
    String userID = (String) session.getAttribute("userID");
    if (userID == null || userID.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/auth/signin.html");
        return;
    }

    String userName = (String) session.getAttribute("userName");
    int userPoints = 0;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/web_demo?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false", "root", "123456ks");
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

    int currentPage = 1;
    String pageStr = request.getParameter("page");
    if (pageStr != null && !pageStr.isEmpty()) {
        try {
            currentPage = Integer.parseInt(pageStr);
            if (currentPage < 1) currentPage = 1;
        } catch (NumberFormatException e) {
            currentPage = 1;
        }
    }

    List<Map<String, Object>> goods = new ArrayList<>();
    int totalGoods = 0;
    int pageSize = 9;
    int totalPages = 1;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/web_demo?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false", "root", "123456ks");
        PreparedStatement countPstmt = conn.prepareStatement("SELECT COUNT(*) FROM goods");
        ResultSet countRs = countPstmt.executeQuery();
        if (countRs.next()) {
            totalGoods = countRs.getInt(1);
        }
        countRs.close();
        countPstmt.close();

        totalPages = (totalGoods + pageSize - 1) / pageSize;
        int offset = (currentPage - 1) * pageSize;
        String sql = "SELECT itemID, itemName, needPoint, `desc` FROM goods LIMIT ? OFFSET ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, pageSize);
        pstmt.setInt(2, offset);
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            Map<String, Object> item = new HashMap<>();
            item.put("itemID", rs.getInt("itemID"));
            item.put("itemName", rs.getString("itemName"));
            item.put("needPoint", rs.getInt("needPoint"));
            item.put("desc", rs.getString("desc"));
            goods.add(item);
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
    <title>积分商城</title>
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
            max-width: 1200px;
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
        .goods-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 30px;
            margin-bottom: 50px;
        }
        .goods-item {
            background-color: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s;
        }
        .goods-item:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
        }
        .goods-image {
            width: 100%;
            height: 200px;
            background-color: #f0f0f0;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }
        .goods-image img {
            max-width: 100%;
            max-height: 100%;
            object-fit: cover;
        }
        .goods-info {
            padding: 20px;
        }
        .goods-name {
            font-size: 18px;
            font-weight: bold;
            color: #333;
            margin-bottom: 10px;
        }
        .goods-points {
            font-size: 16px;
            color: #ff9800;
            font-weight: bold;
            margin-bottom: 15px;
        }
        .goods-desc {
            font-size: 12px;
            color: #999;
            margin-bottom: 15px;
            min-height: 30px;
        }
        .exchange-btn {
            width: 100%;
            padding: 10px;
            background-color: #0088ff;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .exchange-btn:hover {
            background-color: #0077ee;
        }
        .exchange-btn:disabled {
            background-color: #ccc;
            cursor: not-allowed;
        }
        .pagination {
            display: flex;
            justify-content: center;
            gap: 10px;
            margin: 50px 0;
            align-items: center;
        }
        .pagination a, .pagination span {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            text-decoration: none;
            color: #333;
        }
        .pagination a:hover {
            background-color: #0088ff;
            color: white;
        }
        .pagination .active {
            background-color: #0088ff;
            color: white;
        }
        @media (max-width: 1024px) {
            .goods-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }
        @media (max-width: 768px) {
            .goods-grid {
                grid-template-columns: 1fr;
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
    <h1 class="page-title">积分商城</h1>
    <% if (goods.isEmpty()) { %>
        <div style="text-align: center; padding: 50px; color: #999;">暂无商品</div>
    <% } else { %>
        <div class="goods-grid">
            <% for (Map<String, Object> item : goods) { %>
                <div class="goods-item">
                    <div class="goods-image">
                        <img src="${pageContext.request.contextPath}/images/example.webp" alt="<%= item.get("itemName") %>">
                    </div>
                    <div class="goods-info">
                        <div class="goods-name"><%= item.get("itemName") %></div>
                        <div class="goods-points">所需积分：<%= item.get("needPoint") %></div>
                        <div class="goods-desc"><%= item.get("desc") %></div>
                        <button class="exchange-btn" onclick="exchangeGoods(<%= item.get("itemID") %>, <%= item.get("needPoint") %>)" <% if (userPoints < (int)item.get("needPoint")) { %>disabled<% } %>>兑换</button>
                    </div>
                </div>
            <% } %>
        </div>
        <% if (totalPages > 1) { %>
            <div class="pagination">
                <% if (currentPage > 1) { %>
                    <a href="?page=1">首页</a>
                    <a href="?page=<%= currentPage - 1 %>">上一页</a>
                <% } %>
                <% for (int i = 1; i <= totalPages; i++) { %>
                    <% if (i == currentPage) { %>
                        <span class="active"><%= i %></span>
                    <% } else { %>
                        <a href="?page=<%= i %>"><%= i %></a>
                    <% } %>
                <% } %>
                <% if (currentPage < totalPages) { %>
                    <a href="?page=<%= currentPage + 1 %>">下一页</a>
                    <a href="?page=<%= totalPages %>">末页</a>
                <% } %>
            </div>
        <% } %>
    <% } %>
</div>
<script>
    function exchangeGoods(itemID, needPoints) {
        if (confirm('确定要兑换该商品吗？')) {
            let xhr = new XMLHttpRequest();
            xhr.open("POST", "${pageContext.request.contextPath}/exchangeGoods", true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    try {
                        let result = JSON.parse(xhr.responseText);
                        alert(result.msg);
                        if (result.success) {
                            location.reload();
                        }
                    } catch (e) {
                        alert("响应解析失败");
                    }
                }
            };
            xhr.send("itemID=" + itemID + "&needPoints=" + needPoints);
        }
    }
</script>
</body>
</html>

