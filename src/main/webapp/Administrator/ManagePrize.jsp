<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*" %>
<%@ page import="org.example.util.DBUtil" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>学习社区 · 管理后台 - 奖品管理</title>
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
    /* 导航高亮样式补全 */
    .nav-center a.active {
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

    /* 页面标题与标签栏 */
    .page-header {
      margin-bottom: 25px;
    }

    .page-title {
      font-size: 20px;
      font-weight: bold;
      margin-bottom: 15px;
      color: #ffffff;
    }

    .tab-group {
      display: flex;
      background-color: rgba(255, 255, 255, 0.05);
      border-radius: 6px;
      padding: 4px;
      width: fit-content;
    }

    .tab-item {
      padding: 8px 20px;
      border-radius: 4px;
      font-size: 14px;
      color: #999;
      cursor: pointer;
      transition: all 0.3s;
    }

    .tab-item.active {
      background-color: #0088ff;
      color: white;
    }

    .tab-item:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }

    /* 奖品表格 */
    .prize-table {
      width: 100%;
      background-color: rgba(255, 255, 255, 0.05);
      border-radius: 8px;
      overflow: hidden;
      border-collapse: collapse;
    }

    .prize-table th, .prize-table td {
      padding: 12px 15px;
      text-align: left;
      font-size: 14px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    .prize-table th {
      background-color: rgba(255, 255, 255, 0.08);
      color: #cccccc;
      font-weight: 500;
    }

    .prize-table td {
      color: #ffffff;
    }

    .status-active {
      color: #52c41a;
    }
    /* 下架状态样式补充，视觉更完整 */
    .status-inactive {
      color: #ff4d4f;
    }
    /* 新增下架按钮样式 - 匹配管理员深色风格 */
    .btn-del {
      padding: 4px 12px;
      background-color: #ff4d4f;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 12px;
      cursor: pointer;
      transition: background-color 0.3s;
    }
    .btn-del:hover {
      background-color: #ff7875;
    }
    /* 空数据提示样式统一 */
    .empty-tip {
      text-align: center;
      padding: 40px 0;
      color: #999;
    }
  </style>

  <script>
    // 标签切换逻辑保持不变
    function switchTab(tabName) {
      document.querySelectorAll('.tab-item').forEach(item => {
        item.classList.remove('active');
      });
      document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    }
    window.onload = function() {
      switchTab('list');
    }
  </script>
</head>
<body>
<!-- 顶部导航保持不变 -->
<div class="top-nav">
  <div class="nav-left">
    <div class="logo">学习社区 · 管理后台</div>
  </div>
  <div class="nav-center">
    <a href="index.jsp">首页</a>
    <a href="ManagePrize.jsp" class="active">奖品管理</a>
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

<!-- 主内容区 -->
<div class="main-content">
  <div class="page-header">
    <div class="page-title">奖品管理</div>
    <div class="tab-group">
      <div class="tab-item" data-tab="add" onclick="window.location.href='AddPrize.html'">添加奖品</div>
      <div class="tab-item" data-tab="list" onclick="switchTab('list')">奖品列表</div>
      <div class="tab-item" data-tab="off" onclick="switchTab('off')">下架奖品</div>
    </div>
  </div>

  <!-- 奖品列表表格 - 新增操作列+下架按钮 -->
  <table class="prize-table">
    <thead>
    <tr>
      <th>奖品ID</th>
      <th>奖品名称</th>
      <th>商品描述</th>
      <th>所需积分</th>
      <th>操作</th> <!-- 新增操作列 -->
    </tr>
    </thead>
    <tbody>
    <%
      Connection conn = null;
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
        conn = DBUtil.getConnection();
        if (conn == null) {
    %>
    <tr><td colspan="5" class="empty-tip" style="color:#ff4d4f">数据库连接失败，请检查DBUtil配置！</td></tr>
    <%
        return;
      }
      // SQL保持不变，查询所有奖品字段
      String sql = "SELECT itemID as goodsID, itemName as goodsName, needPoint as needPoints, `desc` FROM goods";
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      boolean hasData = false;
      while (rs.next()) {
        hasData = true;
        String goodsID = rs.getString("goodsID");
        String goodsName = rs.getString("goodsName");
        int needPoints = rs.getInt("needPoints");
        String desc = rs.getString("desc");
    %>
    <tr>
      <td><%= goodsID %></td>
      <td><%= goodsName %></td>
      <td><%= desc != null ? desc : "" %></td>
      <td><%= needPoints %></td>
      <!-- 新增下架按钮，携带goodsID跳转到删除确认页 -->
      <td>
        <a href="DeletePrize.jsp?goodsID=<%= goodsID %>">
          <button class="btn-del">下架</button>
        </a>
      </td>
    </tr>
    <%
      }
      if (!hasData) {
    %>
    <tr>
      <td colspan="5" class="empty-tip">暂无奖品数据，点击「添加奖品」上架新奖品吧！</td>
    </tr>
    <%
      }
    } catch (SQLException e) {
    %>
    <tr>
      <td colspan="5" class="empty-tip" style="color:#ff4d4f">数据加载失败：<%= e.getMessage() %></td>
    </tr>
    <%
        e.printStackTrace();
      } finally {
        DBUtil.close(conn, pstmt, rs);
      }
    %>
    </tbody>
  </table>
</div>
</body>
</html>