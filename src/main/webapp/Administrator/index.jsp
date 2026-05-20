<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/mobile.css">
    <title>学习社区 · 管理后台</title>
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

        /* 顶部导航 */
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

        .nav-center a:hover {
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
        }

        .page-title {
            font-size: 20px;
            font-weight: bold;
            margin-bottom: 30px;
            color: #ffffff;
        }

        /* 数据概览卡片 */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background-color: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
            padding: 20px;
            text-align: center;
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }

        .stat-card .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #ffffff;
            margin-bottom: 5px;
        }

        .stat-card .stat-label {
            font-size: 14px;
            color: #999999;
        }

        /* 功能模块 */
        .module-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 20px;
        }

        .module-card {
            background-color: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
            padding: 25px;
            border: 1px solid rgba(255, 255, 255, 0.05);
        }

        .module-card .module-title {
            font-size: 16px;
            font-weight: 500;
            margin-bottom: 10px;
            color: #ffffff;
        }

        .module-card .module-desc {
            font-size: 12px;
            color: #999999;
            margin-bottom: 15px;
            line-height: 1.4;
        }

        .module-card .btn-group {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .btn-primary {
            padding: 6px 14px;
            background-color: #0088ff;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 12px;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        .btn-primary:hover {
            background-color: #0077ee;
        }

        .module-card .count {
            font-size: 12px;
            color: #666666;
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
        <a href="index.jsp" class="active">首页</a>
        <a href="${pageContext.request.contextPath}/material/audit">资料审核</a>
        <a href="${pageContext.request.contextPath}/discussion/audit">问题审核</a>
        <a href="ModifyUserPoints.jsp">用户积分管理</a>
        <a href="ManagePrize.jsp">积分商品管理</a>
        <a href="AddPrize.html">添加商品</a>
    </div>
    <div class="nav-right">
        <a href="signin.html" onclick="return confirm('确定要退出登录吗？')">退出登录</a>
    </div>
</div>
<%
    String userName = (String) session.getAttribute("userName");
    if (userName == null || userName.isEmpty()) {
        userName = "用户";
    }
%>
<!-- 主内容区 -->
<div class="main-content">
    <div class="page-title">管理后台总览</div>

    <!-- 数据概览 -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-value">248</div>
            <div class="stat-label">累计用户数</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">1,256</div>
            <div class="stat-label">已审核资料</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">12</div>
            <div class="stat-label">待审核问题</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">3,892</div>
            <div class="stat-label">总评论数</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">27</div>
            <div class="stat-label">待处理评论</div>
        </div>
        <div class="stat-card">
            <div class="stat-value">86</div>
            <div class="stat-label">已兑换商品</div>
        </div>
    </div>

    <!-- 功能模块 -->
    <div class="module-grid">
        <div class="module-card">
            <div class="module-title">资料审核</div>
            <div class="module-desc">审核用户上传的学习资料，通过后在资料区显示</div>
            <div class="btn-group">
                <a href="${pageContext.request.contextPath}/material/audit" class="btn-primary" style="text-decoration: none; color: white;">进入审核</a>
            </div>
        </div>

        <div class="module-card">
            <div class="module-title">问题审核</div>
            <div class="module-desc">审核用户发布的讨论问题，通过后在讨论区显示</div>
            <div class="btn-group">
                <a href="${pageContext.request.contextPath}/discussion/audit" class="btn-primary" style="text-decoration: none; color: white;">进入审核</a>
            </div>
        </div>

        <div class="module-card">
            <div class="module-title">用户积分管理</div>
            <div class="module-desc">查询、增加、减少或设置用户的积分</div>
            <div class="btn-group">
                <a href="ModifyUserPoints.jsp" class="btn-primary" style="text-decoration: none; color: white;">进入管理</a>
            </div>
        </div>

        <div class="module-card">
            <div class="module-title">积分商品管理</div>
            <div class="module-desc">查看、下架积分商城中的商品</div>
            <div class="btn-group">
                <a href="ManagePrize.jsp" class="btn-primary" style="text-decoration: none; color: white;">进入管理</a>
            </div>
        </div>

        <div class="module-card">
            <div class="module-title">添加商品</div>
            <div class="module-desc">向积分商城添加新的商品</div>
            <div class="btn-group">
                <a href="AddPrize.html" class="btn-primary" style="text-decoration: none; color: white;">进入管理</a>
            </div>
        </div>
    </div>
    <script>
        window.onload = function() {
            alert("欢迎回来，尊贵的管理员<%= userName %>！");
        }
    </script>
</div>
</body>
</html>