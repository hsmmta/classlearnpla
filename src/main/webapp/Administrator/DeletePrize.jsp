<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.example.util.DBUtil" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>学习社区 · 管理后台 - 商品下架确认</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: "Microsoft YaHei", sans-serif;
        }

        body {
            background-color: #12121a;
            color: #ffffff;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        /* 顶部导航 - 与ManagePrize.jsp完全一致 */
        .top-nav {
            background-color: #1e1e2d;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }

        .nav-left .logo {
            font-size: 18px;
            font-weight: bold;
            color: #0088ff;
        }

        .nav-center {
            display: flex;
            gap: 25px;
        }

        .nav-center a {
            color: #cccccc;
            text-decoration: none;
            font-size: 14px;
            transition: color 0.3s;
        }

        .nav-center a:hover, .nav-center a.active {
            color: #0088ff;
        }

        .nav-right a {
            color: #cccccc;
            text-decoration: none;
            font-size: 14px;
        }

        /* 主内容区 */
        .main-content {
            flex: 1;
            padding: 30px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }

        /* 确认卡片 - 核心样式 */
        .confirm-card {
            width: 100%;
            max-width: 500px;
            background-color: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
            padding: 30px;
            box-shadow: 0 0 16px rgba(0, 0, 0, 0.1);
        }

        /* 页面标题 */
        .page-title {
            font-size: 20px;
            font-weight: bold;
            margin-bottom: 25px;
            color: #ffffff;
            text-align: center;
        }

        /* 商品信息展示 */
        .prize-info {
            margin-bottom: 30px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 6px;
            padding: 20px;
        }

        .info-item {
            display: flex;
            margin-bottom: 12px;
            font-size: 14px;
        }
        .info-item:last-child {
            margin-bottom: 0;
        }

        .info-label {
            width: 100px;
            color: #cccccc;
            font-weight: 500;
        }
        .info-value {
            color: #ffffff;
            flex: 1;
        }

        /* 操作按钮组 */
        .btn-group {
            display: flex;
            justify-content: center;
            gap: 20px;
        }

        /* 确定下架按钮 - 红色主按钮 */
        .btn-confirm-del {
            padding: 8px 30px;
            background-color: #ff4d4f;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .btn-confirm-del:hover {
            background-color: #ff7875;
        }

        /* 取消按钮 - 灰色副按钮 */
        .btn-cancel {
            padding: 8px 30px;
            background-color: rgba(255, 255, 255, 0.08);
            color: #cccccc;
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
        }
        .btn-cancel:hover {
            background-color: rgba(255, 255, 255, 0.15);
            color: #ffffff;
        }

        /* 错误提示 */
        .error-tip {
            color: #ff4d4f;
            font-size: 14px;
            text-align: center;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
<!-- 顶部导航 -->
<div class="top-nav">
    <div class="nav-left">
        <div class="logo">学习社区 · 管理后台</div>
    </div>
    <div class="nav-center">
        <a href="index.jsp">首页</a>
        <a href="ManagePrize.jsp" class="active">商品管理</a>
        <a href="#">用户管理</a>
        <a href="#">资料管理</a>
        <a href="#">问题管理</a>
        <a href="#">评论管理</a>
        <a href="#">积分管理</a>
    </div>
    <div class="nav-right">
        <a href="${pageContext.request.contextPath}/Administrator/signin.html" onclick="return confirm('确定要退出登录吗？')">退出登录</a>
    </div>
</div>

<!-- 主内容区 - 下架确认逻辑 -->
<div class="main-content">
    <div class="confirm-card">
        <h2 class="page-title">商品下架确认</h2>
        <%
        // 1. 获取URL传递的商品ID
        String goodsID = request.getParameter("goodsID");
        // 2. 校验商品ID是否为空
        if (goodsID == null || goodsID.trim().isEmpty()) {
        %>
        <div class="error-tip">错误：未获取到商品ID，无法执行下架操作！</div>
        <div class="btn-group">
            <a href="ManagePrize.jsp" class="btn-cancel">返回商品列表</a>
        </div>
        <%
        return;
        }
        goodsID = goodsID.trim();

        // 3. 声明数据库变量
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 4. 判断是否为「确定下架」提交的请求（通过form的del参数判断）
        String delFlag = request.getParameter("del");
        if ("true".equals(delFlag)) {
        // ------------- 执行下架删除逻辑 -------------
        try {
        conn = DBUtil.getConnection();
        if (conn == null) {
        %>
        <div class="error-tip">数据库连接失败，下架操作失败！</div>
        <%
        } else {
        // 删除SQL：根据goodsID删除商品
        String delSql = "DELETE FROM goodslist WHERE goodsID = ?";
        pstmt = conn.prepareStatement(delSql);
        pstmt.setString(1, goodsID);
        int rows = pstmt.executeUpdate();
        if (rows > 0) {
        // 删除成功：弹窗提示并跳转回商品列表
        response.getWriter().write("<script>alert('商品下架成功！'); window.location.href='ManagePrize.jsp';</script>");
        return;
        } else {
        // 删除失败：无此商品
        %>
        <div class="error-tip">下架失败：未找到该商品（可能已被下架）！</div>
        <%
        }
        }
        } catch (SQLException e) {
        e.printStackTrace();
        %>
        <div class="error-tip">下架失败：数据库错误 - <%= e.getMessage() %></div>
        <%
        } finally {
        DBUtil.close(conn, pstmt);
        }
        } else {
        // ------------- 首次访问：查询并展示商品详情 -------------
        try {
        conn = DBUtil.getConnection();
        if (conn == null) {
        %>
        <div class="error-tip">数据库连接失败，无法获取商品信息！</div>
        <div class="btn-group">
            <a href="ManagePrize.jsp" class="btn-cancel">返回商品列表</a>
        </div>
        <%
        return;
        }
        // 查询商品详情SQL
        String selectSql = "SELECT goodsName, goodsType, needPoints, currentNum FROM goodslist WHERE goodsID = ?";
        pstmt = conn.prepareStatement(selectSql);
        pstmt.setString(1, goodsID);
        rs = pstmt.executeQuery();
        if (rs.next()) {
        // 获取商品信息
        String goodsName = rs.getString("goodsName");
        String goodsType = rs.getString("goodsType");
        int needPoints = rs.getInt("needPoints");
        int currentNum = rs.getInt("currentNum");
        %>
        <!-- 展示商品详细信息 -->
        <div class="prize-info">
            <div class="info-item">
                <span class="info-label">商品ID：</span>
                <span class="info-value"><%= goodsID %></span>
            </div>
            <div class="info-item">
                <span class="info-label">商品名称：</span>
                <span class="info-value"><%= goodsName %></span>
            </div>
            <div class="info-item">
                <span class="info-label">商品类型：</span>
                <span class="info-value"><%= goodsType %></span>
            </div>
            <div class="info-item">
                <span class="info-label">所需积分：</span>
                <span class="info-value"><%= needPoints %></span>
            </div>
            <div class="info-item">
                <span class="info-label">库存数量：</span>
                <span class="info-value"><%= currentNum %></span>
            </div>
        </div>
        <!-- 确认下架表单：提交del=true表示执行删除 -->
        <form method="post" action="DeletePrize.jsp?goodsID=<%= goodsID %>">
            <input type="hidden" name="del" value="true">
            <div class="btn-group">
                <button type="submit" class="btn-confirm-del">确定下架</button>
                <a href="ManagePrize.jsp" class="btn-cancel">取消</a>
            </div>
        </form>
        <%
        } else {
        // 未查询到商品
        %>
        <div class="error-tip">未找到商品ID为【<%= goodsID %>】的商品！</div>
        <div class="btn-group">
            <a href="ManagePrize.jsp" class="btn-cancel">返回商品列表</a>
        </div>
        <%
        }
        } catch (SQLException e) {
        e.printStackTrace();
        %>
        <div class="error-tip">获取商品信息失败：<%= e.getMessage() %></div>
        <div class="btn-group">
            <a href="ManagePrize.jsp" class="btn-cancel">返回商品列表</a>
        </div>
        <%
        } finally {
        DBUtil.close(conn, pstmt, rs);
        }
        }
        %>
    </div>
</div>
</body>
</html>