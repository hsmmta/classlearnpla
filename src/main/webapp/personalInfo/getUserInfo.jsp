<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%
    // 获取用户积分
    String userID = (String) session.getAttribute("userID");
    int userPoints = 0;

    if (userID != null && !userID.isEmpty()) {
        // 从points表查询用户积分
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/web_demo?characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false", "root", "123456ks");
            String sql = "SELECT points FROM points WHERE userID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userPoints = rs.getInt("points");
            } else {
                // 如果points表中没有该用户，则插入一条记录
                String insertSql = "INSERT INTO points (userID, userName, points) VALUES (?, ?, 0)";
                PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
                insertPstmt.setString(1, userID);
                insertPstmt.setString(2, (String) session.getAttribute("userName"));
                insertPstmt.executeUpdate();
                insertPstmt.close();
                userPoints = 0;
            }

            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            userPoints = 0; // 如果查询失败，默认为0
        }
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>班级学习社区平台-个人信息</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: "Microsoft YaHei", sans-serif;
        }
        body {
            background-color: #f0f4f9;
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
        }
        .nav-bar a {
            text-decoration: none;
            color: #333;
            font-size: 16px;
            transition: color 0.3s;
        }
        .nav-bar a:hover {
            color: #0088ff;
        }
        .nav-bar .active {
            color: #0088ff;
            font-weight: bold;
        }
        .info-container {
            width: 500px;
            margin: 0 auto;
            background-color: white;
            border-radius: 12px;
            padding: 30px;
            box-shadow: 0 0 16px rgba(0, 0, 0, 0.08);
        }
        .info-title {
            font-size: 20px;
            font-weight: bold;
            color: #222;
            margin-bottom: 25px;
            padding-bottom: 10px;
            border-bottom: 1px solid #f0f0f0;
        }
        .info-item {
            display: flex;
            margin: 20px 0;
            font-size: 14px;
        }
        .info-label {
            width: 100px;
            color: #666;
            text-align: right;
            padding-right: 20px;
        }
        .info-value {
            color: #333;
            flex: 1;
        }
        /* 新增：空值提示样式 */
        .info-value.empty {
            color: #999;
        }
        .btn-group {
            margin-top: 30px;
            display: flex;
            gap: 15px;
        }
        .btn {
            padding: 8px 18px;
            border: 1px solid #ddd;
            border-radius: 6px;
            background-color: white;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s;
        }
        .btn-primary {
            border-color: #0088ff;
            color: #0088ff;
        }
        .btn-primary:hover {
            background-color: #0088ff;
            color: white;
        }
        .btn-danger {
            border-color: #ff4d4f;
            background-color: #ff4d4f;
            color: white;
        }
        .btn-danger:hover {
            background-color: #ff7875;
        }
    </style>
    <script>
        // 修改：注销账号改为跳转页面（删除原有的异步请求逻辑）
        function deleteAccount() {
            // 可选：保留一级轻提示，确认是否要进入注销页面（也可以直接跳转）
            if (confirm('您确定要进入账号注销页面吗？注销后数据将无法恢复！')) {
                // 跳转到cancelUser.html（注意路径要和你的实际文件位置匹配）
                window.location.href = "${pageContext.request.contextPath}/personalInfo/cancelUser.html";
            }
        }
    </script>
</head>
<body>
<!-- 导航栏 -->
<div class="nav-bar">
    <ul>
        <li><a href="${pageContext.request.contextPath}/home/index.jsp">首页</a></li>
        <li><a href="${pageContext.request.contextPath}/material/search">资料区</a></li>
        <li><a href="${pageContext.request.contextPath}/discussion/list">讨论区</a></li>
        <li><a href="${pageContext.request.contextPath}/Prize/DisplayPrize.jsp">积分商城</a></li>
        <li><a href="${pageContext.request.contextPath}/personalInfo/getUserInfo.jsp" class="active">个人中心</a></li>
        <li><a href="${pageContext.request.contextPath}/auth/signin.html" onclick="return confirm('确定要退出登录吗？')">退出登录</a></li>
    </ul>
</div>

<div class="info-container">
    <div class="info-title">我的个人信息</div>

    <!-- 用户ID（手机号）：匹配Session中的userID（原phone） -->
    <div class="info-item">
        <span class="info-label">用户ID</span>
        <span class="info-value" id="userID">
            ${empty sessionScope.userID ? sessionScope.phone : sessionScope.userID}
        </span>
    </div>
    <!-- 昵称 -->
    <div class="info-item">
        <span class="info-label">昵称</span>
        <span class="info-value" id="username">
            ${empty sessionScope.userName ? '<span class="empty">未设置</span>' : sessionScope.userName}
        </span>
    </div>
    <!-- 班级编号 -->
    <div class="info-item">
        <span class="info-label">班级编号</span>
        <span class="info-value" id="classID">
            ${empty sessionScope.classID ? '<span class="empty">未设置</span>' : sessionScope.classID}
        </span>
    </div>
    <div class="info-item">
        <span class="info-label">性别</span>
        <span class="info-value" id="gender">
            <%
                String gender = (String) session.getAttribute("gender");
                if (gender == null || gender.trim().isEmpty()) {
                    out.print("<span class='empty'>未设置</span>");
                } else if ("male".equals(gender)) {
                    out.print("男");
                } else if ("female".equals(gender)) {
                    out.print("女");
                } else {
                    out.print("<span class='empty'>未知</span>");
                }
            %>
        </span>
    </div>
    <!-- 学号 -->
    <div class="info-item">
        <span class="info-label">学号</span>
        <span class="info-value" id="studentID">
            ${empty sessionScope.studentID ? '<span class="empty">未设置</span>' : sessionScope.studentID}
        </span>
    </div>
    <!-- 邮箱 -->
    <div class="info-item">
        <span class="info-label">邮箱</span>
        <span class="info-value" id="email">
            ${empty sessionScope.userEmail ? '<span class="empty">未设置</span>' : sessionScope.userEmail}
        </span>
    </div>
    <!-- 个人积分 -->
    <div class="info-item">
        <span class="info-label">个人积分</span>
        <span class="info-value" id="points" style="color: #ff9800; font-weight: bold;">
            <%= userPoints %>
        </span>
    </div>

    <div class="btn-group">
        <button class="btn btn-primary" onclick="window.location.href='${pageContext.request.contextPath}/personalInfo/changeUserInfo.jsp'">修改信息</button>
        <button class="btn btn-primary" onclick="window.location.href='${pageContext.request.contextPath}/personalInfo/changePassword.html'">修改密码</button>
        <button class="btn btn-secondary" onclick="window.location.href='${pageContext.request.contextPath}/personalInfo/forgetPassword.jsp'">找回密码</button>
        <!-- 按钮点击事件不变，只是JS函数逻辑改了 -->
        <button class="btn btn-danger" onclick="deleteAccount()">注销账号</button>
    </div>
</div>

</body>
</html>